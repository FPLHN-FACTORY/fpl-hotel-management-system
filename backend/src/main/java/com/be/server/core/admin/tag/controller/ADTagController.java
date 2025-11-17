package com.be.server.core.admin.tag.controller;

import com.be.server.core.admin.tag.model.request.ADAddAndUpdateTagRequest;
import com.be.server.core.admin.tag.model.request.ADSearchTagRequest;
import com.be.server.core.admin.tag.service.ADTagService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_LE_TAN_TAG)
public class ADTagController {
    private final ADTagService adTagService;
    @GetMapping()
    public ResponseEntity<?>getAllTags( @ModelAttribute  ADSearchTagRequest request) {
        return Helper.createResponseEntity(adTagService.getAllTags(request));
    }
    @PostMapping("/add-tag")
    public ResponseEntity<?>addTag(@RequestBody ADAddAndUpdateTagRequest request){
        return Helper.createResponseEntity(adTagService.addTag(request));
    }
    @PutMapping("/update-tag/{id}")
    public ResponseEntity<?>updateTag(@RequestBody ADAddAndUpdateTagRequest request,@PathVariable  String id){
        return Helper.createResponseEntity(adTagService.updateTag(request,id));
    }
    @PutMapping("/change-status/{id}")
    public ResponseEntity<?>changeStatusTag(@PathVariable String id){
        return Helper.createResponseEntity(adTagService.changeStatusTag(id));
    }
}
