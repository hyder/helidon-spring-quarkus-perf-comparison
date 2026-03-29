package io.helidon.labs.resource;

import java.util.List;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.BadRequestException;
import io.helidon.http.Http;
import io.helidon.http.NotFoundException;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.mapping.FruitMapper;
import io.helidon.labs.repository.FruitRepository;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings("deprecation")
@Http.Path("/fruits")
@Service.Singleton
@RestServer.Endpoint
public class FruitResource {

    private final FruitRepository fruitRepository;

    @Service.Inject
    public FruitResource(FruitRepository fruitRepository) {
        this.fruitRepository = fruitRepository;
    }

    @Http.GET
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<FruitDto> all() {
        return fruitRepository.listDetailedOrderByName().stream()
                .map(FruitMapper::toDto)
                .toList();
    }

    @Http.GET
    @Http.Path("/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    FruitDto fruit(@Http.PathParam("name") String name) {
        return fruitRepository.findDetailedByName(name)
                .map(FruitMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Fruit not found: " + name));
    }

    @Http.POST
    @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Tx.Required
    FruitDto insert(@Http.Entity FruitCreateRequest request) {
        FruitCreateRequest normalizedRequest = normalize(request);
        fruitRepository.findByName(normalizedRequest.name())
                .ifPresent(existing -> {
                    throw new BadRequestException("Fruit already exists: " + normalizedRequest.name());
                });
        Fruit fruit = fruitRepository.insert(FruitMapper.toEntity(normalizedRequest));
        return FruitMapper.toDto(fruit);
    }

    private static FruitCreateRequest normalize(FruitCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Fruit payload is required.");
        }
        String name = request.name() == null ? null : request.name().trim();
        if (name == null || name.isEmpty()) {
            throw new BadRequestException("Fruit name is required.");
        }
        String description = request.description() == null ? null : request.description().trim();
        return new FruitCreateRequest(name, description);
    }
}
