package com.tbf.tcms.web;

import com.tbf.tcms.domain.User;
import com.tbf.tcms.service.UserService;
import com.tbf.tcms.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Operations for listing, creating users and managing council/roles")
public class UserController {

    private final UserService userService;

    // --- READ: Paged users ---
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<User>> listUsers(
            @PageableDefault(size = 20, sort = {"fullName"}) Pageable pageable
    ) {
        PageResponse<User> page = userService.findAll(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.totalElements()))
                .body(page);
    }

    // Example: Ntona viewing all eligible council members in a village
    @GetMapping("/eligible-council")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<User>> listEligibleCouncil(
            @RequestParam Long orgId,
            @PageableDefault(size = 100, sort = {"fullName"}) Pageable pageable
    ) {
        PageResponse<User> page = userService.findEligibleCouncilByOrganization(orgId, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.totalElements()))
                .body(page);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<User> createUser(@RequestParam String fullName,
                                           @RequestParam String lineage,
                                           @RequestParam Long organizationId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate) {
        User created = userService.createUser(fullName, lineage, organizationId, birthDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{userId}/disqualify")
    @PreAuthorize("hasRole('ADMIN')")
    public User disqualify(@PathVariable Long userId, @RequestParam String reason) {
        return userService.disqualifyUser(userId, reason);
    }

    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public User assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        return userService.assignRoleToUser(userId, roleName);
    }

    @PostMapping("/council/appoint-top")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> appointTopCouncil(@RequestParam Long orgId, @RequestParam(defaultValue = "10") int size) {
        return userService.appointTopCouncil(orgId, size);
    }

    @PostMapping("/{userId}/council/appoint")
    @PreAuthorize("hasRole('ADMIN')")
    public User appointUserToCouncil(@PathVariable Long userId) {
        return userService.appointUserToCouncil(userId);
    }

    @PostMapping("/{leaderId}/heir/{heirUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public User defineHeir(@PathVariable Long leaderId, @PathVariable Long heirUserId) {
        return userService.defineHeir(leaderId, heirUserId);
    }
}
