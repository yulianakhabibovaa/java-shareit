package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequestDetailedDto;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toRequestedItemsDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public Collection<ItemRequestDetailedDto> getUserRequests(Long userId) {
        return formatRequests(itemRequestRepository.findAllByRequesterId(userId));
    }

    @Override
    public Collection<ItemRequestDetailedDto> getOtherUsersRequests(Long userId) {
        return formatRequests(itemRequestRepository.findAllByRequesterIdNotIn(Set.of(userId)));
    }

    @Override
    public ItemRequestDetailedDto create(ItemRequestDto request) {
        User requester = userRepository.findById(request.getRequester())
                .orElseThrow(() -> new UserNotFoundException(request.getRequester()));
        ItemRequest itemRequest = itemRequestRepository.save(
                ItemRequest.builder()
                        .description(request.getDescription())
                        .requester(requester)
                        .created(LocalDateTime.now(ZoneId.of("Europe/Moscow")))
                        .build()
        );
        log.info("Создан запрос на предмет: {}", itemRequest);
        return toItemRequestDetailedDto(itemRequest);
    }

    @Override
    public ItemRequestDetailedDto getRequest(Long itemRequestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new ItemRequestNotFoundException(itemRequestId));

        Collection<Item> items = itemRepository.findAllByRequestId(itemRequest.getId());
        return toItemRequestDetailedDto(itemRequest).withItems(toRequestedItemsDto(items));
    }

    private Collection<ItemRequestDetailedDto> formatRequests(Collection<ItemRequest> itemRequests) {
        Collection<Long> itemRequestIds = itemRequests.stream().map(ItemRequest::getId).toList();
        Collection<Item> items = itemRepository.findAllByRequestIdIn(itemRequestIds);

        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequests.stream()
                .map(itemRequest -> toItemRequestDetailedDto(itemRequest)
                        .withItems(toRequestedItemsDto(itemsByRequestId.getOrDefault(itemRequest.getId(), List.of())))
                )
                .toList();
    }
}
