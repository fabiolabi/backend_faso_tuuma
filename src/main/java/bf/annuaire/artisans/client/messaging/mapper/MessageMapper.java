package bf.annuaire.artisans.client.messaging.mapper;

import bf.annuaire.artisans.client.messaging.dto.MessageDto;
import bf.annuaire.artisans.client.messaging.entity.Message;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping {@link Message} → {@link MessageDto} (MapStruct). */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderUserId", source = "sender.id")
    MessageDto toDto(Message message);

    List<MessageDto> toDtoList(List<Message> messages);
}
