package com.nimmda.web.admin;

import com.nimmda.application.admin.AdminDirectoryService;
import com.nimmda.application.admin.AdminOverview;
import com.nimmda.application.admin.AdminUserView;
import com.nimmda.application.business.BusinessSubscriptionView;
import com.nimmda.application.business.ListBusinessSubscriptionsService;
import com.nimmda.application.promotion.ListPromotionPaymentsService;
import com.nimmda.application.promotion.PromotionPaymentView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminDirectoryService adminDirectoryService;
    private final ListPromotionPaymentsService listPromotionPaymentsService;
    private final ListBusinessSubscriptionsService listBusinessSubscriptionsService;

    public AdminController(
            AdminDirectoryService adminDirectoryService,
            ListPromotionPaymentsService listPromotionPaymentsService,
            ListBusinessSubscriptionsService listBusinessSubscriptionsService
    ) {
        this.adminDirectoryService = adminDirectoryService;
        this.listPromotionPaymentsService = listPromotionPaymentsService;
        this.listBusinessSubscriptionsService = listBusinessSubscriptionsService;
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
    public List<PromotionPaymentView> advertisers() {
        return listPromotionPaymentsService.execute();
    }

    @GetMapping("/subscriptions")
    public List<BusinessSubscriptionView> subscriptions() {
        return listBusinessSubscriptionsService.execute();
    }
}
