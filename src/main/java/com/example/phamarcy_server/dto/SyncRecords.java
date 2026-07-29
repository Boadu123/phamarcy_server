package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SyncRecords(
        @Valid
        List<UserSyncItem> users,
        @Valid
        List<ProductSyncItem> products,
        @Valid
        List<BatchSyncItem> batches,
        @Valid
        List<SaleSyncItem> sales,
        @JsonProperty("sale_items")
        @Valid
        List<SaleItemSyncItem> saleItems,
        @JsonProperty("app_settings")
        @Valid
        List<AppSettingSyncItem> appSettings
) {
    public SyncRecords {
        users = copyOf(users);
        products = copyOf(products);
        batches = copyOf(batches);
        sales = copyOf(sales);
        saleItems = copyOf(saleItems);
        appSettings = copyOf(appSettings);
    }

    public static SyncRecords empty() {
        return new SyncRecords(null, null, null, null, null, null);
    }

    private static <T> List<T> copyOf(List<T> items) {
        return items == null ? List.of() : List.copyOf(items);
    }
}
