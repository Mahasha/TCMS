package com.tbf.tcms.config;

import com.tbf.tcms.domain.*;
import com.tbf.tcms.domain.enums.CaseStatus;
import com.tbf.tcms.domain.enums.StandType;
import com.tbf.tcms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.List;

@Configuration
@Slf4j
public class DatabaseSeeder {

    // We use a @Bean method instead of implementing the interface on the class directly.
    // This allows us to inject repositories easily as arguments.

    @Bean
    @Profile("!prod")
        // Safety mechanism: Never run this in production!
    CommandLineRunner initDatabase(
            OrganizationRepository orgRepo,
            RoleRepository roleRepo,
            UserRepository userRepo,
            LandStandRepository landStandRepo,
            DisputeCaseRepository disputeRepo
    ) {
        return args -> {
            if (roleRepo.count() > 0) {
                log.info("Database already seeded. Skipping...");
                return;
            }

            log.info("Seeding database...");

            // 1. Create Organizations
            Organization royalHouse = new Organization("Royal House", "Monarchy", null);
            Organization northernCouncil = new Organization("Mothomeng village", "Regional", royalHouse);

            orgRepo.saveAll(List.of(royalHouse, northernCouncil));

            // 2. Create Roles
            Role roleAdmin = new Role("ADMIN", "System Administrator");
            Role roleChief = new Role("CHIEF", "Traditional Leader");
            Role roleCitizen = new Role("CITIZEN", "Standard User");

            roleRepo.saveAll(List.of(roleAdmin, roleChief, roleCitizen));

            // 3. Create Users
            // King Lekukela
            User king = new User("King Lekukela", "Royal Bloodline", royalHouse);
            king.setBirthDate(LocalDate.of(1948, 7, 14));
            king.addRole(roleAdmin);
            king.addRole(roleChief);

            // Chief Mahasha
            User chief = new User("Chief Mahasha", "Mahasha Clan", northernCouncil);
            chief.setBirthDate(LocalDate.of(1955, 9, 1));
            chief.setHeirTo(king); // Just as an example relationship
            chief.addRole(roleChief);

            // Citizen Sipho
            User sipho = new User("Sipho Modika", "Modika Clan", northernCouncil);
            sipho.setBirthDate(LocalDate.of(1990, 5, 15));
            sipho.addRole(roleCitizen);

            userRepo.saveAll(List.of(king, chief, sipho));

            // 4. Create Land Stands
            LandStand stand1 = new LandStand();
            stand1.setStandNumber("STAND-001");
            stand1.setType(StandType.RESIDENTIAL);
            stand1.setSizeInSquareMeters(500.0);
            stand1.setAllocated(true);
            stand1.setAllocationDate(LocalDate.now().minusMonths(6));
            stand1.setAllocatedTo(sipho);
            stand1.setOrganization(northernCouncil);

            landStandRepo.save(stand1);

            // 5. Create Dispute Case
            DisputeCase dispute = new DisputeCase();
            dispute.setDescription("Boundary dispute regarding STAND-001");
            dispute.setOpenedDate(LocalDate.now().minusDays(10));
            dispute.setStatus(CaseStatus.OPEN);
            dispute.setComplainant(sipho);
            dispute.setAccusedUser(chief); // Sipho complaining about the Chief!
            dispute.setOrganization(northernCouncil);

            // Add adjudicators
            dispute.getAdjudicators().add(king); // The King will judge

            disputeRepo.save(dispute);

            log.info("Database seeding completed successfully.");
        };
    }
}
