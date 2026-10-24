package com.petconnect.service;

import com.petconnect.dto.CareBookingRequestDto;
import com.petconnect.dto.ShelterCareSearchResultDto;
import com.petconnect.entity.CareBooking;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface CareBookingService {

    List<ShelterCareSearchResultDto> searchShelters(String location, LocalDate startDate, LocalDate endDate, String petType);

    int calculateAvailableCapacity(Shelter shelter, LocalDate startDate, LocalDate endDate);

    int getCurrentOccupancy(Shelter shelter);

    CareBooking submitCareRequest(User user, CareBookingRequestDto requestDto);

    CareBooking acceptRequest(Long bookingId, User shelterOwner);

    CareBooking rejectRequest(Long bookingId, String reason, User shelterOwner);

    CareBooking cancelBooking(Long bookingId, User user);

    CareBooking checkInPet(Long bookingId, User shelterOwner);

    CareBooking checkOutPet(Long bookingId, User shelterOwner);

    List<CareBooking> getUserBookings(User user);

    List<CareBooking> getShelterRequests(Shelter shelter);

    List<CareBooking> getCurrentStays(Shelter shelter);

    CareBooking getBookingById(Long id);
}
