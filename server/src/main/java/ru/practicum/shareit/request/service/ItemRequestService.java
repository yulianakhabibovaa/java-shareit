package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

@Service
public interface ItemRequestService {
    Collection<ItemRequestDetailedDto> getUserRequests(Long userId);

    ItemRequestDetailedDto create(ItemRequestDto request);

    ItemRequestDetailedDto getRequest(Long itemRequestId);

    Collection<ItemRequestDetailedDto> getOtherUsersRequests(Long userId);
}