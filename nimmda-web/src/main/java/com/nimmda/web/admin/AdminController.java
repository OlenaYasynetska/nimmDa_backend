package com.nimmda.web.admin;

import com.nimmda.application.admin.AdminDirectoryService;
import com.nimmda.application.admin.AdminOverview;
import com.nimmda.application.admin.AdminUserView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminDirectoryService adminDirectoryService;

    public AdminController(AdminDirectoryService adminDirectoryService) {
        this.adminDirectoryService = adminDirectoryService;
    }

    @GetMapping("/overview")
    public AdminOverview overview() {
        return adminDirectoryService.overview();
    }

    @GetMapping("/users")
    public List<AdminUserView> users() {
        return adminDirectoryService.users();
    }

    @GetMapping("/payments")
    public List<Object> payments() {
        return List.of();
    }

    @GetMapping("/advertisers")
    public List<Object> advertisers() {
        return List.of();
    }

    @GetMapping("/subscriptions")
    public List<Object> subscriptions() {
        return List.of();
    }
}
