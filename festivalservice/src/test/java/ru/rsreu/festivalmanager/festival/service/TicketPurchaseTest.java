package ru.rsreu.festivalmanager.festival.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rsreu.festivalmanager.festival.client.UserServiceClient;
import ru.rsreu.festivalmanager.festival.dto.festival.UserProfileDto;
import ru.rsreu.festivalmanager.festival.dto.ticket.TicketPurchaseRequest;
import ru.rsreu.festivalmanager.festival.entity.festival.AgeLimit;
import ru.rsreu.festivalmanager.festival.entity.festival.Festival;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalInstance;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;
import ru.rsreu.festivalmanager.festival.entity.ticket.Ticket;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketType;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketTypeInstance;
import ru.rsreu.festivalmanager.festival.mapper.TicketMapper;
import ru.rsreu.festivalmanager.festival.mapper.TicketMapperImpl;
import ru.rsreu.festivalmanager.festival.repository.FestivalCompanyRepository;
import ru.rsreu.festivalmanager.festival.repository.FestivalCuratorRepository;
import ru.rsreu.festivalmanager.festival.repository.FestivalInstanceRepository;
import ru.rsreu.festivalmanager.festival.repository.FestivalLocationRepository;
import ru.rsreu.festivalmanager.festival.repository.TicketRepository;
import ru.rsreu.festivalmanager.festival.repository.TicketTypeInstanceRepository;
import ru.rsreu.festivalmanager.festival.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Покупка билета")
class TicketPurchaseTest {

    private static final String BEARER = "Bearer system-token";
    private static final long USER_ID = 42L;
    private static final long INSTANCE_ID = 100L;
    private static final long TICKET_TYPE_INSTANCE_ID = 55L;

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private TicketTypeInstanceRepository ticketTypeInstanceRepository;
    @Mock private FestivalCompanyRepository festivalCompanyRepository;
    @Mock private FestivalInstanceRepository festivalInstanceRepository;
    @Mock private FestivalLocationRepository festivalLocationRepository;
    @Mock private FestivalCuratorRepository festivalCuratorRepository;
    @Mock private UserServiceClient userClient;

    private final TicketMapper ticketMapper = new TicketMapperImpl();
    private TicketService service;

    @BeforeEach
    void setUp() {
        service = new TicketService(ticketRepository, ticketMapper, ticketTypeRepository,
                ticketTypeInstanceRepository, festivalCompanyRepository, festivalInstanceRepository,
                festivalLocationRepository, festivalCuratorRepository, userClient);
        ReflectionTestUtils.setField(service, "systemToken", "system-token");
    }

    private static FestivalInstance instance(String ageLimit, boolean visible, int maxPeople) {
        FestivalInstance instance = new FestivalInstance();
        instance.setId(INSTANCE_ID);
        instance.setAgeLimit(new AgeLimit(1, ageLimit));
        instance.setVisible(visible);
        instance.setMaxPeopleNumber(maxPeople);
        instance.setDateStart(LocalDate.of(2026, 7, 1));
        instance.setTimeStart(LocalTime.of(10, 0));
        instance.setDateEnd(LocalDate.of(2026, 7, 3));
        instance.setTimeEnd(LocalTime.of(22, 0));
        instance.setFestival(new Festival(1L, "ФестХаб"));
        return instance;
    }

    private static TicketTypeInstance ticketTypeInstance(FestivalInstance festivalInstance, BigDecimal price) {
        FestivalLocation location = new FestivalLocation();
        location.setId(11L);
        location.setFestivalInstance(festivalInstance);

        TicketTypeInstance instance = new TicketTypeInstance();
        instance.setId(TICKET_TYPE_INSTANCE_ID);
        instance.setPrice(price);
        instance.setDescription("Стандарт");
        instance.setFestivalLocation(location);
        instance.setTicketType(new TicketType(1L, "Стандартный"));
        return instance;
    }

    private static UserProfileDto profile(LocalDate birth, String phone, String passport) {
        return UserProfileDto.builder()
                .id(USER_ID)
                .nickname("tester")
                .dateOfBirth(birth)
                .phone(phone)
                .passportData(passport)
                .build();
    }

    private static TicketPurchaseRequest request(int quantity) {
        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setTicketTypeInstanceId(TICKET_TYPE_INSTANCE_ID);
        request.setQuantity(quantity);
        return request;
    }

    /** Общая обвязка: не в чёрном списке, тип билета и фестиваль найдены. */
    private FestivalInstance arrange(String ageLimit, boolean visible, int maxPeople,
                                     BigDecimal price, UserProfileDto userProfile, int soldTickets) {
        FestivalInstance festivalInstance = instance(ageLimit, visible, maxPeople);
        when(userClient.checkBlackListStatus(BEARER, USER_ID)).thenReturn(false);
        when(ticketTypeInstanceRepository.findById(TICKET_TYPE_INSTANCE_ID))
                .thenReturn(Optional.of(ticketTypeInstance(festivalInstance, price)));
        when(userClient.getUserProfile(BEARER, USER_ID)).thenReturn(userProfile);
        when(festivalInstanceRepository.findByIdWithPessimisticLock(INSTANCE_ID))
                .thenReturn(Optional.of(festivalInstance));
        when(ticketRepository.sumSoldTicketsByFestivalInstanceId(INSTANCE_ID)).thenReturn(soldTickets);
        return festivalInstance;
    }

    private static LocalDate age(int years) {
        return LocalDate.now().minusYears(years).minusDays(1);
    }

    @Nested
    @DisplayName("Проверки покупателя")
    class BuyerChecks {

        @Test
        @DisplayName("пользователь из чёрного списка билет не купит")
        void rejectsBlacklistedUser() {
            when(userClient.checkBlackListStatus(BEARER, USER_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("черном списке");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("без даты рождения купить нельзя")
        void requiresBirthDate() {
            arrange("18+", true, 100, BigDecimal.TEN, profile(null, "+79990000000", "4600 123456"), 0);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("дату рождения");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("возраст ниже ограничения отклоняется с указанием возраста")
        void rejectsUnderageBuyer() {
            arrange("18+", true, 100, BigDecimal.TEN, profile(age(16), "+79990000000", "4600 123456"), 0);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("16")
                    .hasMessageContaining("18+");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("без телефона купить нельзя")
        void requiresPhone() {
            arrange("18+", true, 100, BigDecimal.TEN, profile(age(25), "   ", "4600 123456"), 0);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("номер телефона");
        }

        @Test
        @DisplayName("на фестиваль 16+ и старше нужен паспорт")
        void requiresPassportForAgeRestrictedFestival() {
            arrange("16+", true, 100, BigDecimal.TEN, profile(age(25), "+79990000000", null), 0);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("паспортные данные");
        }

        @Test
        @DisplayName("на фестиваль 12+ паспорт не нужен")
        void doesNotRequirePassportBelowSixteen() {
            arrange("12+", true, 100, BigDecimal.TEN, profile(age(14), "+79990000000", null), 0);

            service.purchaseTicket(USER_ID, request(1));

            verify(ticketRepository).save(any(Ticket.class));
        }
    }

    @Nested
    @DisplayName("Доступность билетов")
    class Availability {

        @Test
        @DisplayName("на неопубликованный фестиваль продажи закрыты")
        void rejectsInvisibleFestival() {
            arrange("0+", false, 100, BigDecimal.TEN, profile(age(25), "+79990000000", null), 0);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Продажа билетов");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("нельзя купить больше, чем осталось, и остаток сообщается")
        void rejectsPurchaseAboveRemaining() {
            arrange("0+", true, 100, BigDecimal.TEN, profile(age(25), "+79990000000", null), 98);

            assertThatThrownBy(() -> service.purchaseTicket(USER_ID, request(3)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Осталось: 2");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("ровно последние билеты купить можно")
        void allowsBuyingExactlyRemaining() {
            arrange("0+", true, 100, BigDecimal.TEN, profile(age(25), "+79990000000", null), 98);

            service.purchaseTicket(USER_ID, request(2));

            verify(ticketRepository).save(any(Ticket.class));
        }
    }

    @Nested
    @DisplayName("Создание билета")
    class TicketCreation {

        @Test
        @DisplayName("итоговая цена — цена типа, умноженная на количество")
        void multipliesPriceByQuantity() {
            arrange("0+", true, 100, new BigDecimal("150.50"),
                    profile(age(25), "+79990000000", null), 0);

            service.purchaseTicket(USER_ID, request(3));

            ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(saved.capture());
            assertThat(saved.getValue().getFinalPrice()).isEqualByComparingTo("451.50");
            assertThat(saved.getValue().getQuantity()).isEqualTo(3);
            assertThat(saved.getValue().getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("срок действия билета совпадает с датами фестиваля")
        void takesValidityFromFestivalDates() {
            arrange("0+", true, 100, BigDecimal.TEN, profile(age(25), "+79990000000", null), 0);

            service.purchaseTicket(USER_ID, request(1));

            ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(saved.capture());
            assertThat(saved.getValue().getStartDate())
                    .isEqualTo(LocalDateTime.of(2026, 7, 1, 10, 0));
            assertThat(saved.getValue().getExpiryDate())
                    .isEqualTo(LocalDateTime.of(2026, 7, 3, 22, 0));
        }

        @Test
        @DisplayName("нестандартное ограничение по возрасту не блокирует покупку")
        void toleratesUnparsableAgeLimit() {
            arrange("без ограничений", true, 100, BigDecimal.TEN,
                    profile(age(10), "+79990000000", null), 0);

            service.purchaseTicket(USER_ID, request(1));

            verify(ticketRepository).save(any(Ticket.class));
        }
    }
}
