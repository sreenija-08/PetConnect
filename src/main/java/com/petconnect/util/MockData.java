package com.petconnect.util;

import com.petconnect.dto.PetDto;
import com.petconnect.dto.ShelterDto;
import com.petconnect.dto.AdoptionRequestDto;
import com.petconnect.dto.CareBookingDto;

import java.util.ArrayList;
import java.util.List;

public class MockData {

    public static List<PetDto> getPets() {
        List<PetDto> list = new ArrayList<>();
        list.add(new PetDto(1L, "Bruno", "DOG", "Labrador", 2, "Male", 
                "Bruno is a friendly, energetic Labrador who loves playing fetch and getting belly rubs. He gets along well with people and other pets.",
                "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=800",
                "Fully vaccinated, neutered, and dewormed.", "APPROVED", "AVAILABLE", "Happy Paws Shelter", null));
        
        list.add(new PetDto(2L, "Luna", "CAT", "Persian", 1, "Female", 
                "Luna is a quiet and affectionate Persian cat with a beautiful fluffy coat. She loves lounging in warm sun spots.",
                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&q=80&w=800",
                "Vaccinated and spayed.", "APPROVED", "AVAILABLE", "Safe Tails Shelter", null));

        list.add(new PetDto(3L, "Max", "DOG", "Beagle", 3, "Male", 
                "Max is an active Beagle who loves sniffing trails and running around. He is very playful and kid-friendly.",
                "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=800",
                "Fully vaccinated and neutered.", "APPROVED", "AVAILABLE", "Paws & Claws", null));

        list.add(new PetDto(4L, "Milo", "CAT", "Ragdoll", 1, "Male", 
                "Milo is a gentle Ragdoll cat who loves to be held. He has striking blue eyes and a soft color-point coat.",
                "https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&q=80&w=800",
                "Vaccinated and neutered.", "APPROVED", "AVAILABLE", "Happy Paws Shelter", null));

        list.add(new PetDto(5L, "Rocky", "DOG", "German Shepherd", 3, "Male", 
                "Rocky is a loyal and intelligent German Shepherd. He is well-trained, alert, and protective.",
                "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?auto=format&fit=crop&q=80&w=800",
                "Vaccinated, neutered, and microchipped.", "APPROVED", "AVAILABLE", "Second Chance Shelter", "Sreenija"));

        list.add(new PetDto(6L, "Simba", "CAT", "Maine Coon", 2, "Male", 
                "Simba is a large, majestic Maine Coon. He is friendly, curious, and enjoys being around his human companions.",
                "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&q=80&w=800",
                "Vaccinated and neutered.", "APPROVED", "AVAILABLE", "Whisker's Home", null));

        list.add(new PetDto(7L, "Buddy", "DOG", "Golden Retriever", 4, "Male", 
                "Buddy is a classic Golden Retriever who loves swimming and retrieving toys. He is exceptionally sweet-tempered.",
                "https://images.unsplash.com/photo-1561037404-61cd46aa615b?auto=format&fit=crop&q=80&w=800",
                "Fully vaccinated and neutered.", "APPROVED", "AVAILABLE", "Safe Tails Shelter", null));

        list.add(new PetDto(8L, "Coco", "CAT", "Siamese", 1, "Female", 
                "Coco is an elegant Siamese cat with striking blue eyes. She is vocal, playful, and highly interactive.",
                "https://images.unsplash.com/photo-1592194996308-7b43878e84a6?auto=format&fit=crop&q=80&w=800",
                "Vaccinated and spayed.", "APPROVED", "AVAILABLE", "Safe Tails Shelter", null));
        
        return list;
    }

    public static List<ShelterDto> getShelters() {
        List<ShelterDto> list = new ArrayList<>();
        list.add(new ShelterDto(1L, "Happy Paws Shelter", "Chennai, Tamil Nadu", 
                "We rescue and care for abandoned and rescued animals, helping them find loving forever homes.", 
                20, 5, "APPROVED", true, "Rahul", "9876543210", "happy@paws.com"));
        list.add(new ShelterDto(2L, "Safe Tails Shelter", "Chennai, Tamil Nadu", 
                "Providing temporary care, medical aid, and adoption support for stray and abandoned pets.", 
                15, 3, "APPROVED", true, "Priya", "9876543211", "safe@tails.com"));
        list.add(new ShelterDto(3L, "Paws & Claws", "Chennai, Tamil Nadu", 
                "A loving community sanctuary providing high-quality boarding and adoption services for pets.", 
                10, 8, "APPROVED", true, "Karthik", "9876543212", "paws@claws.com"));
        return list;
    }

    public static List<AdoptionRequestDto> getAdoptionRequests() {
        List<AdoptionRequestDto> list = new ArrayList<>();
        list.add(new AdoptionRequestDto(1L, "Sreenija", "Bruno", "Labrador", "DOG", "Happy Paws Shelter", "20 May 2026", "APPROVED", "I want to adopt a pet to join our family.", "Please arrange a check-in appointment."));
        list.add(new AdoptionRequestDto(2L, "Anu", "Luna", "Persian", "CAT", "Safe Tails Shelter", "18 May 2026", "PENDING", "I have a quiet apartment and love cats.", "Under admin review."));
        list.add(new AdoptionRequestDto(3L, "Karthik", "Max", "Beagle", "DOG", "Paws & Claws", "16 May 2026", "REJECTED", "I want a guard dog for my shop.", "The environment is not suited for a Beagle."));
        return list;
    }

    public static List<CareBookingDto> getCareBookings() {
        List<CareBookingDto> list = new ArrayList<>();
        list.add(new CareBookingDto(1L, "Rocky", "DOG", "German Shepherd", "Sreenija", "Happy Paws Shelter", "05 May 2026", "08 May 2026", "APPROVED", "Needs feeding twice a day.", "01 May 2026"));
        list.add(new CareBookingDto(2L, "Charlie", "DOG", "Indie", "Anu", "Safe Tails Shelter", "10 May 2026", "15 May 2026", "PENDING", "Requires regular medication.", "05 May 2026"));
        list.add(new CareBookingDto(3L, "Buddy", "DOG", "Golden Retriever", "Karthik", "Happy Paws Shelter", "15 May 2026", "18 May 2026", "APPROVED", "LOVES fetching tennis balls.", "10 May 2026"));
        list.add(new CareBookingDto(4L, "Max", "CAT", "Persian", "Divya", "Paws & Claws", "20 May 2026", "23 May 2026", "REJECTED", "Requires quiet space.", "12 May 2026"));
        return list;
    }
}
