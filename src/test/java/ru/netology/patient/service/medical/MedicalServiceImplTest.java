package ru.netology.patient.service.medical;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MedicalServiceImplTest {
    public static List<PatientInfo> TEST_PATIENTS = new ArrayList<>() {{
        add(new PatientInfo("21f89cee-c0ae-4eb9-b291-f0dffd12e16b",
                "Юрий", "Сидоров", LocalDate.of(1981, 2, 26),
                new HealthInfo(new BigDecimal("36.55"), new BloodPressure(120, 80)))
        );
        add(new PatientInfo("dccdf215-1dfc-4165-979a-f53e0f95d36e",
                "Сергей", "Смирнов", LocalDate.of(1982, 5, 21),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(130, 90)))
        );
    }};
    MedicalService medicalService;
    PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoRepository.class);

    @BeforeAll
    public void setup() {
        TEST_PATIENTS.forEach(e -> Mockito.when(
                patientInfoRepository.getById(e.getId()))
                .thenReturn(e));
    }

    private static Stream<PatientInfo> providePatients() {
        return TEST_PATIENTS.stream();
    }

    @BeforeEach
    public void init() {
        SendAlertService alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @ParameterizedTest
    @MethodSource("providePatients")
    public void testCheckBloodPressureNoAlert(PatientInfo patientInfo) {
        SendAlertService alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        BloodPressure currentBloodPressure = patientInfo.getHealthInfo().getBloodPressure();

        medicalService.checkBloodPressure(patientInfo.getId(), currentBloodPressure);
        Mockito.verify(alertService, never()).send(Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("providePatients")
    public void testCheckTemperatureNoAlert(PatientInfo patientInfo) {
        SendAlertService alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        BigDecimal currentTemperature = patientInfo.getHealthInfo().getNormalTemperature();

        medicalService.checkTemperature(patientInfo.getId(), currentTemperature);
        Mockito.verify(alertService, never()).send(Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("providePatients")
    public void testCheckBloodPressureAlert(PatientInfo patientInfo) {
        SendAlertService alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        int normalHighPressure = patientInfo.getHealthInfo().getBloodPressure().getHigh();
        int normalLowPressure = patientInfo.getHealthInfo().getBloodPressure().getLow();
        BloodPressure currentBloodPressure = new BloodPressure(normalHighPressure + 10, normalLowPressure + 10);

        medicalService.checkBloodPressure(patientInfo.getId(), currentBloodPressure);
        Mockito.verify(alertService, times(1)).send(Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("providePatients")
    public void testCheckTemperatureAlert(PatientInfo patientInfo) {
        SendAlertService alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        BigDecimal currentTemperature = patientInfo.getHealthInfo().getNormalTemperature().subtract(BigDecimal.valueOf(2.0));

        medicalService.checkTemperature(patientInfo.getId(), currentTemperature);
        Mockito.verify(alertService, times(1)).send(Mockito.any());
    }
}
