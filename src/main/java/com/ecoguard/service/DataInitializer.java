package com.ecoguard.service;

import com.ecoguard.model.StatusHistory;
import com.ecoguard.model.User;
import com.ecoguard.model.WasteCategory;
import com.ecoguard.model.WasteReport;
import com.ecoguard.repository.StatusHistoryRepository;
import com.ecoguard.repository.UserRepository;
import com.ecoguard.repository.WasteCategoryRepository;
import com.ecoguard.repository.WasteReportRepository;
import com.ecoguard.util.SecurityUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WasteCategoryRepository categoryRepository;
    private final WasteReportRepository reportRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public DataInitializer(UserRepository userRepository,
                           WasteCategoryRepository categoryRepository,
                           WasteReportRepository reportRepository,
                           StatusHistoryRepository statusHistoryRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.reportRepository = reportRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCategories();
        seedReports();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            userRepository.save(new User(
                    "Admin Officer (Wetland Authority)",
                    "admin@ecoguard.org",
                    SecurityUtils.hashPassword("admin123"),
                    "ADMIN"
            ));
            userRepository.save(new User(
                    "Abhinav Roopesh (Eco Volunteer)",
                    "volunteer@ecoguard.org",
                    SecurityUtils.hashPassword("volunteer123"),
                    "VOLUNTEER"
            ));
            userRepository.save(new User(
                    "Adarsh S (Citizen Reporter)",
                    "citizen@gmail.com",
                    SecurityUtils.hashPassword("citizen123"),
                    "CITIZEN"
            ));
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new WasteCategory("Plastic Waste", "PLASTIC", "Single-use plastics, packaging materials, and discarded bottles", "bottle-water"));
            categoryRepository.save(new WasteCategory("Industrial Runoff", "INDUSTRIAL", "Chemical effluents, toxic oils, and unverified industrial discharge", "industry"));
            categoryRepository.save(new WasteCategory("Electronic Waste", "EWASTE", "Batteries, electronic boards, wire casings with toxic heavy metals", "microchip"));
            categoryRepository.save(new WasteCategory("Bio / Organic Waste", "ORGANIC", "Market and banquet organic refuse, decomposing matter", "leaf"));
            categoryRepository.save(new WasteCategory("Construction Debris", "CONSTRUCTION", "Concrete chunks, gravel, demolition rubble dumped on wetland fringes", "trowel-bricks"));
            categoryRepository.save(new WasteCategory("General Solid Waste", "GENERAL", "Mixed roadside garbage, paper piles, unsegregated solid trash", "trash-can"));
        }
    }

    private void seedReports() {
        if (reportRepository.count() == 0) {
            createSampleReport(
                    "Plastic Waste",
                    "Massive accumulation of single-use plastic bottles, snack packets, and beverage cups floating near culvert 4 beneath the 200 Feet Radial Road flyover. Obstructs rainwater drainage into marsh core.",
                    "200 Feet Radial Road, near Flyover Pillar #14",
                    "Zone A - Radial Road Corridor",
                    "HIGH",
                    "SUBMITTED",
                    "Kavitha Sundaram",
                    "kavitha.s@gmail.com",
                    "+91 98412 34567",
                    "Initial report logged via EcoGuard web portal."
            );

            createSampleReport(
                    "Construction Debris",
                    "Two truckloads of broken concrete slabs, plaster, and ceramic tiles unlawfully unloaded onto the protected wetland boundary strip opposite Perumbakkam housing colony.",
                    "Perumbakkam Wetland Border Road, Sector 3",
                    "Zone C - Perumbakkam Boundary",
                    "CRITICAL",
                    "IN_PROGRESS",
                    "Ramesh Chandran",
                    "ramesh.c@chennaiwatch.org",
                    "+91 94440 98765",
                    "Wetland marsh enforcement patrol dispatched with JCB loader."
            );

            createSampleReport(
                    "Industrial Runoff",
                    "Milky chemical liquid with strong solvent odor seeping into marsh creek from unauthorized small manufacturing unit backyards along the southern access road.",
                    "Southern Radial Link, Near Velachery Lake Spillway",
                    "Zone E - Sholinganallur Radial Link",
                    "CRITICAL",
                    "UNDER_REVIEW",
                    "Dr. Vignesh Narayanan",
                    "vignesh.env@iitm.ac.in",
                    "+91 97910 11223",
                    "Pollution Control Board lab sampling team requested."
            );

            createSampleReport(
                    "Plastic Waste",
                    "Polythene carry bags, disposable food containers, and mineral water bottles scattered along the pedestrian boundary near the Velachery MRTS terminal edge.",
                    "Velachery MRTS Terminal Perimeter, Northern Gate",
                    "Zone B - Velachery MRTS Edge",
                    "MEDIUM",
                    "RESOLVED",
                    "Adarsh S",
                    "citizen@gmail.com",
                    "+91 98840 55443",
                    "Sanitation drive completed. 450 kg of recyclable plastics collected."
            );

            createSampleReport(
                    "Electronic Waste",
                    "Old CRT television chassis, melted motherboard remnants, and discarded automotive battery casings dumped in water channel reeds.",
                    "Medavakkam Bypass Road, Culvert #2",
                    "Zone D - Medavakkam Lake Inlet",
                    "HIGH",
                    "IN_PROGRESS",
                    "Senthil Kumar",
                    "senthil.k@techie.com",
                    "+91 91760 33445",
                    "Hazardous waste recovery partner scheduled for immediate collection."
            );

            createSampleReport(
                    "Bio / Organic Waste",
                    "Unsegregated food waste and decaying poultry refuse left open along roadside ditch, attracting stray dogs and flies close to migratory duck feeding pond.",
                    "Kovilambakkam Radial Road Junction",
                    "Zone A - Radial Road Corridor",
                    "MEDIUM",
                    "RESOLVED",
                    "Priya Varadhan",
                    "priya.v@gmail.com",
                    "+91 98401 77889",
                    "Waste cleared and treated with organic sanitizing powder by local panchayat."
            );

            createSampleReport(
                    "General Solid Waste",
                    "Open garbage dumping by commercial roadside tea stalls and tire repair workshops alongside the wetland conservation wall.",
                    "Thoraipakkam 200 Feet Road, West Edge",
                    "Zone E - Sholinganallur Radial Link",
                    "LOW",
                    "SUBMITTED",
                    "Arun Prasath",
                    "arun.p@outlook.com",
                    "+91 90030 44556",
                    "Awaiting inspection by ward officer."
            );

            createSampleReport(
                    "Plastic Waste",
                    "Discarded heavy-gauge plastic sacks filled with factory packaging wrappers floating near freshwater inlet canal.",
                    "Pallikaranai Main Lake Water Inflow Channel",
                    "Zone B - Velachery MRTS Edge",
                    "HIGH",
                    "RESOLVED",
                    "Abhinav Roopesh",
                    "volunteer@ecoguard.org",
                    "+91 99620 12345",
                    "Volunteer cleanup crew removed 12 sacks with wetland ranger guidance."
            );
        }
    }

    private void createSampleReport(String category, String desc, String location, String zone,
                                    String severity, String status, String name, String email,
                                    String phone, String historyNote) {
        WasteReport report = new WasteReport();
        report.setCategory(category);
        report.setDescription(desc);
        report.setLocation(location);
        report.setZone(zone);
        report.setSeverity(severity);
        report.setStatus(status);
        report.setReporterName(name);
        report.setReporterEmail(email);
        report.setReporterPhone(phone);
        report.setCreatedAt(LocalDateTime.now().minusHours((long) (Math.random() * 72 + 2)));
        report.setUpdatedAt(LocalDateTime.now());

        WasteReport saved = reportRepository.save(report);

        // Record history
        StatusHistory h1 = new StatusHistory(saved.getId(), null, "SUBMITTED", name, "Initial submission");
        h1.setUpdatedAt(saved.getCreatedAt());
        statusHistoryRepository.save(h1);

        if (!status.equals("SUBMITTED")) {
            StatusHistory h2 = new StatusHistory(saved.getId(), "SUBMITTED", status, "Admin / Wetland Patrol", historyNote);
            h2.setUpdatedAt(saved.getUpdatedAt());
            statusHistoryRepository.save(h2);
        }
    }
}
