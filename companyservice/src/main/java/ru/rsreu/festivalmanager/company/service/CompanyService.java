package ru.rsreu.festivalmanager.company.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.company.dto.CompanyCreateRequest;
import ru.rsreu.festivalmanager.company.dto.CompanyResponse;
import ru.rsreu.festivalmanager.company.dto.CompanyUpdateRequest;
import ru.rsreu.festivalmanager.company.entity.Company;
import ru.rsreu.festivalmanager.company.event.CompanyEvents;
import ru.rsreu.festivalmanager.company.repository.CompanyRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createInactiveCompany(CompanyCreateRequest request) {
        Optional<Company> existingByTin = companyRepository.findByTin(request.getTin());
        Optional<Company> existingByEmail = companyRepository.findByEmail(request.getEmail());

        Company company;

        if (existingByTin.isPresent() || existingByEmail.isPresent()) {
            company = existingByTin.orElseGet(existingByEmail::get);

            if (company.isActive()) {
                throw new IllegalArgumentException("Компания с такими данными уже существует и активна");
            }

            company.setFullName(request.getFullName());
            company.setBrandName(request.getBrandName());
            company.setTin(request.getTin());
            company.setEmail(request.getEmail());
            company.setWebsite(request.getWebsite());
            log.info("Обновление данных неактивной компании ID: {} перед повторной подачей", company.getId());

        } else {
            company = new Company();
            company.setTin(request.getTin());
            company.setFullName(request.getFullName());
            company.setBrandName(request.getBrandName());
            company.setEmail(request.getEmail());
            company.setWebsite(request.getWebsite());
            company.setActive(false);
            log.info("Создание новой неактивной компании");
        }

        company = companyRepository.save(company);

        eventPublisher.publishEvent(new CompanyEvents.CompanyAppRequestEvent(
                company.getId(),
                company.getFullName(),
                OffsetDateTime.now()
        ));

        return company.getId();
    }

    @Transactional
    public void updateEmail(Long id, String newEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));

        company.setEmail(newEmail);

        companyRepository.save(company);
    }

    public CompanyResponse getCompanyByTin(String tin) {
        Company company = companyRepository.findByTin(tin)
                .orElseThrow(() -> new IllegalArgumentException("Компания с ИНН " + tin + " не найдена"));
        return mapToResponse(company);
    }

    @Transactional
    public void activateCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));
        company.setActive(true);
        companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));

        company.setFullName(request.getFullName());
        company.setBrandName(request.getBrandName());
        company.setWebsite(request.getWebsite());

        return mapToResponse(companyRepository.save(company));
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .tin(company.getTin())
                .fullName(company.getFullName())
                .brandName(company.getBrandName())
                .email(company.getEmail())
                .website(company.getWebsite())
                .isActive(company.isActive())
                .build();
    }
}
