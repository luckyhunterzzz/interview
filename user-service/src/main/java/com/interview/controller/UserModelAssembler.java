package com.interview.controller;

import com.interview.dto.UserResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {
        return EntityModel.of(user,
                linkTo(methodOn(UserHateoasController.class).getUserById(user.id())).withSelfRel(),
                linkTo(methodOn(UserHateoasController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserHateoasController.class).updateUser(user.id(), null)).withRel("update"),
                linkTo(methodOn(UserHateoasController.class).deleteUser(user.id())).withRel("delete"));
    }

    public CollectionModel<EntityModel<UserResponse>> toCollectionModel(List<UserResponse> users) {
        List<EntityModel<UserResponse>> userModels = users.stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(userModels,
                linkTo(methodOn(UserHateoasController.class).getAllUsers()).withSelfRel());
    }
}