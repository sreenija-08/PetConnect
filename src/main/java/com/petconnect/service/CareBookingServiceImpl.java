package com.petconnect.service;

import com.petconnect.dto.CareBookingRequestDto;
import com.petconnect.dto.ShelterCareSearchResultDto;
import com.petconnect.entity.*;
import com.petconnect.repository.CareBookingRepository;
import com.petconnect.repository.PetRepository;
import com.petconnect.repository.ShelterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CareBookingServiceImpl implements CareBookingService {

    private final CareBookingRepository careBookingRepository;
    private final ShelterRepository shelterRepository;
    private final PetRepository petRepository;
    private final NotificationService notificationService;

    @Autowired
    public CareBookingServiceImpl(CareBookingRepository careBookingRepository,
                                  ShelterRepository shelterRepository,
                                  PetRepository petRepository,
                                  NotificationService notificationService) {
        this.careBookingRepository = careBookingRepository;
        this.shelterRepository = shelterRepository;
        this.petRepository = petRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<ShelterCareSearchResultDto> searchShelters(String location, LocalDate startDate, LocalDate endDate, String petType) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().plusDays(1);
        LocalDate end = endDate != null ? endDate : start.plusDays(3);

        if (!end.isAfter(start)) {
            end = start.plusDays(1);
        }

        List<Shelter> allShelters = shelterRepository.findByVerificationStatus("APPROVED");
        if (allShelters.isEmpty()) {
            allShelters = shelterRepository.findAll();
        }

        final LocalDate queryStart = start;
        final LocalDate queryEnd = end;
        final String queryType = petType != null && !petType.isBlank() ? petType.trim().toUpperCase() : "ALL";

        return allShelters.stream()
                .filter(Shelter::isActive)
                .filter(Shelter::isTemporaryCareEnabled)
                .filter(s -> location == null || location.isBlank() || 
                        (s.getLocation() != null && s.getLocation().toLowerCase().contains(location.toLowerCase())) ||
                        (s.getName() != null && s.getName().toLowerCase().contains(location.toLowerCase())))
                .filter(s -> s.supportsPetType(queryType))
                .map(shelter -> {
                    int available = calculateAvailableCapacity(shelter, queryStart, queryEnd);
                    int total = shelter.getCapacity() != null ? shelter.getCapacity() : 20;
                    double fee = shelter.getBoardingFee();
                    int days = (int) Math.max(1, ChronoUnit.DAYS.between(queryStart, queryEnd));
                    double totalFee = days * fee;
                    boolean hasCap = available > 0;

                    return new ShelterCareSearchResultDto(
                            shelter,
                            total,
                            available,
                            fee,
                            totalFee,
                            days,
                            hasCap,
                            queryStart,
                            queryEnd,
                            queryType
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public int calculateAvailableCapacity(Shelter shelter, LocalDate startDate, LocalDate endDate) {
        if (shelter == null) return 0;
        int totalCapacity = shelter.getCapacity() != null ? shelter.getCapacity() : 20;

        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : start.plusDays(1);
        if (!end.isAfter(start)) {
            end = start.plusDays(1);
        }

        // Active bookings that occupy capacity
        List<CareBookingStatus> capacityStatuses = List.of(
                CareBookingStatus.ACCEPTED,
                CareBookingStatus.CHECKED_IN,
                CareBookingStatus.ACTIVE
        );

        List<CareBooking> overlapping = careBookingRepository.findOverlappingShelterBookings(
                shelter, start, end, capacityStatuses
        );

        // Find peak occupancy across all dates in [start, end)
        int maxOccupancy = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            final LocalDate day = current;
            int count = (int) overlapping.stream()
                    .filter(b -> !b.getStartDate().isAfter(day) && !b.getEndDate().isBefore(day))
                    .count();
            if (count > maxOccupancy) {
                maxOccupancy = count;
            }
            current = current.plusDays(1);
        }

        return Math.max(0, totalCapacity - maxOccupancy);
    }

    @Override
    public int getCurrentOccupancy(Shelter shelter) {
        if (shelter == null) return 0;
        List<CareBooking> active = careBookingRepository.findByShelterAndStatusInOrderByRequestedAtDesc(
                shelter, List.of(CareBookingStatus.ACTIVE, CareBookingStatus.CHECKED_IN)
        );
        return active.size();
    }

    @Override
    @Transactional
    public CareBooking submitCareRequest(User user, CareBookingRequestDto dto) {
        if (user == null) {
            throw new IllegalArgumentException("User must be authenticated to request pet care.");
        }
        if (dto.getPetId() == null) {
            throw new IllegalArgumentException("Please select your pet.");
        }
        if (dto.getShelterId() == null) {
            throw new IllegalArgumentException("Please select a shelter.");
        }

        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Pet not found with ID: " + dto.getPetId()));

        // Verify pet belongs to logged-in user
        boolean isOwner = (pet.getUser() != null && pet.getUser().getId().equals(user.getId())) ||
                (pet.getOwnerName() != null && (pet.getOwnerName().equalsIgnoreCase(user.getFullName()) || pet.getOwnerName().equalsIgnoreCase(user.getEmail())));
        if (!isOwner) {
            throw new SecurityException("You can only request care for your own registered pets.");
        }

        Shelter shelter = shelterRepository.findById(dto.getShelterId())
                .orElseThrow(() -> new IllegalArgumentException("Shelter not found with ID: " + dto.getShelterId()));

        if (!shelter.isTemporaryCareEnabled()) {
            throw new IllegalStateException("Temporary pet care is currently disabled for this shelter.");
        }

        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Please provide both start and end dates.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be after the start date.");
        }

        // Prevent conflicting bookings for the same pet
        List<CareBookingStatus> blockingStatuses = List.of(
                CareBookingStatus.PENDING,
                CareBookingStatus.ACCEPTED,
                CareBookingStatus.CHECKED_IN,
                CareBookingStatus.ACTIVE
        );
        List<CareBooking> conflictingPetBookings = careBookingRepository.findOverlappingPetBookings(
                pet, start, end, blockingStatuses
        );
        if (!conflictingPetBookings.isEmpty()) {
            throw new IllegalStateException("This pet already has an active or pending care booking during the selected dates.");
        }

        // Capacity check
        int available = calculateAvailableCapacity(shelter, start, end);
        if (available <= 0) {
            throw new IllegalStateException("Sorry, this shelter has no available boarding capacity during the requested dates.");
        }

        CareBooking booking = new CareBooking();
        booking.setUser(user);
        booking.setPet(pet);
        booking.setShelter(shelter);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setBoardingFeePerDay(shelter.getBoardingFee());
        booking.setEmergencyContact(dto.getEmergencyContact());
        booking.setSpecialInstructions(dto.getSpecialInstructions());
        booking.setMedicalInformation(dto.getMedicalInformation());
        booking.setVaccinationInfo(dto.getVaccinationInfo());
        booking.setAdditionalNotes(dto.getAdditionalNotes());
        booking.setStatus(CareBookingStatus.PENDING);
        booking.setRequestedAt(LocalDateTime.now());
        booking.recalculateFee();

        CareBooking saved = careBookingRepository.save(booking);

        // Notify shelter owner
        if (shelter.getOwner() != null) {
            notificationService.createNotification(
                    shelter.getOwner(),
                    "New Pet Care Request",
                    user.getFullName() + " submitted a boarding request for " + pet.getName() + " (" + start + " to " + end + ").",
                    "CARE"
            );
        }

        // Notify applicant user
        notificationService.createNotification(
                user,
                "Care Request Submitted",
                "Your boarding request for " + pet.getName() + " at " + shelter.getName() + " has been submitted.",
                "CARE"
        );

        return saved;
    }

    @Override
    @Transactional
    public CareBooking acceptRequest(Long bookingId, User shelterOwner) {
        CareBooking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Care booking not found with ID: " + bookingId);
        }

        // Security check
        Shelter shelter = booking.getShelter();
        if (shelter.getOwner() != null && !shelter.getOwner().getId().equals(shelterOwner.getId())) {
            throw new SecurityException("Unauthorized: You do not own this shelter.");
        }

        if (booking.getStatus() != CareBookingStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be accepted.");
        }

        // Re-check capacity immediately before accepting to prevent overbooking
        int available = calculateAvailableCapacity(shelter, booking.getStartDate(), booking.getEndDate());
        if (available <= 0) {
            throw new IllegalStateException("Sorry, this shelter no longer has sufficient capacity for these dates.");
        }

        booking.setStatus(CareBookingStatus.ACCEPTED);
        booking.setApprovedAt(LocalDateTime.now());
        CareBooking saved = careBookingRepository.save(booking);

        // Notify user
        notificationService.createNotification(
                booking.getUser(),
                "Pet Care Request Accepted",
                "Your pet care request for " + booking.getPet().getName() + " has been accepted by " + shelter.getName() + "!",
                "CARE"
        );

        return saved;
    }

    @Override
    @Transactional
    public CareBooking rejectRequest(Long bookingId, String reason, User shelterOwner) {
        CareBooking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Care booking not found with ID: " + bookingId);
        }

        Shelter shelter = booking.getShelter();
        if (shelter.getOwner() != null && !shelter.getOwner().getId().equals(shelterOwner.getId())) {
            throw new SecurityException("Unauthorized: You do not own this shelter.");
        }

        if (booking.getStatus() == CareBookingStatus.ACTIVE || booking.getStatus() == CareBookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reject an active or completed stay.");
        }

        booking.setStatus(CareBookingStatus.REJECTED);
        booking.setRejectedAt(LocalDateTime.now());
        booking.setShelterRemarks(reason);
        CareBooking saved = careBookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUser(),
                "Pet Care Request Declined",
                "Your pet care request for " + booking.getPet().getName() + " was declined." + 
                        (reason != null && !reason.isBlank() ? " Note: " + reason : ""),
                "CARE"
        );

        return saved;
    }

    @Override
    @Transactional
    public CareBooking cancelBooking(Long bookingId, User user) {
        CareBooking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Care booking not found with ID: " + bookingId);
        }

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized: You can only cancel your own bookings.");
        }

        if (booking.getStatus() != CareBookingStatus.PENDING && booking.getStatus() != CareBookingStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot cancel a booking that has already commenced or completed.");
        }

        booking.setStatus(CareBookingStatus.CANCELLED);
        CareBooking saved = careBookingRepository.save(booking);

        if (booking.getShelter().getOwner() != null) {
            notificationService.createNotification(
                    booking.getShelter().getOwner(),
                    "Care Request Cancelled",
                    user.getFullName() + " cancelled their care reservation for " + booking.getPet().getName() + ".",
                    "CARE"
            );
        }

        return saved;
    }

    @Override
    @Transactional
    public CareBooking checkInPet(Long bookingId, User shelterOwner) {
        CareBooking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Care booking not found with ID: " + bookingId);
        }

        Shelter shelter = booking.getShelter();
        if (shelter.getOwner() != null && !shelter.getOwner().getId().equals(shelterOwner.getId())) {
            throw new SecurityException("Unauthorized: You do not own this shelter.");
        }

        if (booking.getStatus() != CareBookingStatus.ACCEPTED && booking.getStatus() != CareBookingStatus.PENDING) {
            throw new IllegalStateException("Only accepted bookings can be checked in.");
        }

        booking.setStatus(CareBookingStatus.ACTIVE);
        booking.setCheckInDateTime(LocalDateTime.now());
        CareBooking saved = careBookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUser(),
                "Pet Checked In",
                booking.getPet().getName() + " has arrived and checked in at " + shelter.getName() + ". Temporary boarding is now active.",
                "CARE"
        );

        return saved;
    }

    @Override
    @Transactional
    public CareBooking checkOutPet(Long bookingId, User shelterOwner) {
        CareBooking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Care booking not found with ID: " + bookingId);
        }

        Shelter shelter = booking.getShelter();
        if (shelter.getOwner() != null && !shelter.getOwner().getId().equals(shelterOwner.getId())) {
            throw new SecurityException("Unauthorized: You do not own this shelter.");
        }

        if (booking.getStatus() != CareBookingStatus.ACTIVE && booking.getStatus() != CareBookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Only currently active stays can be checked out.");
        }

        booking.setStatus(CareBookingStatus.COMPLETED);
        booking.setCheckOutDateTime(LocalDateTime.now());
        CareBooking saved = careBookingRepository.save(booking);

        // Crucial: Pet ownership remains unchanged with the user
        notificationService.createNotification(
                booking.getUser(),
                "Pet Checked Out",
                booking.getPet().getName() + " has been checked out from " + shelter.getName() + " and returned to your custody. Thank you!",
                "CARE"
        );

        return saved;
    }

    @Override
    public List<CareBooking> getUserBookings(User user) {
        if (user == null) return Collections.emptyList();
        return careBookingRepository.findByUserOrderByRequestedAtDesc(user);
    }

    @Override
    public List<CareBooking> getShelterRequests(Shelter shelter) {
        if (shelter == null) return Collections.emptyList();
        return careBookingRepository.findByShelterOrderByRequestedAtDesc(shelter);
    }

    @Override
    public List<CareBooking> getCurrentStays(Shelter shelter) {
        if (shelter == null) return Collections.emptyList();
        return careBookingRepository.findByShelterAndStatusInOrderByRequestedAtDesc(
                shelter, List.of(CareBookingStatus.ACTIVE, CareBookingStatus.CHECKED_IN)
        );
    }

    @Override
    public CareBooking getBookingById(Long id) {
        if (id == null) return null;
        return careBookingRepository.findById(id).orElse(null);
    }
}
