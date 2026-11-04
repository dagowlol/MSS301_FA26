package com.mss301.brand.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    private Integer brandId;
    private String brandName;
    private String countryOfOrigin;
    private Long watchCount;
}
