package com.example.odyssey.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class NftMessageMetadataDTO {
    private String name;

    private String description;

    private Long tokenId;

    private String image;

    private List<NftMessageMetadataDetailDTO> attributes;
}
