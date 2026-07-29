package com.example.phamarcy_server.service.impl;

import com.example.phamarcy_server.dto.SyncEntityResult;
import com.example.phamarcy_server.dto.SyncRecords;
import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.entity.SyncActivity;
import com.example.phamarcy_server.exception.ApiException;
import com.example.phamarcy_server.repository.SyncActivityRepository;
import com.example.phamarcy_server.service.SyncActivityService;
import java.time.Instant;
import java.util.UUID;
import java.util.function.ToIntFunction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncActivityServiceImpl implements SyncActivityService {

    private final SyncActivityRepository syncActivityRepository;

    public SyncActivityServiceImpl(SyncActivityRepository syncActivityRepository) {
        this.syncActivityRepository = syncActivityRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID start(SyncRequest request) {
        SyncRecords records = request.records();
        UUID activityId = UUID.randomUUID();
        syncActivityRepository.save(new SyncActivity(
                activityId,
                request.pharmacyId(),
                Instant.now(),
                totalReceived(records),
                records.products().size() + records.batches().size(),
                records.sales().size()
        ));
        return activityId;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccessful(UUID activityId, SyncResponse response) {
        SyncActivity activity = syncActivityRepository.findById(activityId).orElseThrow();
        activity.markSuccessful(
                Instant.now(),
                sum(response, SyncEntityResult::inserted),
                sum(response, SyncEntityResult::updated),
                sum(response, SyncEntityResult::ignored),
                applied(response.products()) + applied(response.batches()),
                applied(response.sales())
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID activityId, RuntimeException failure) {
        SyncActivity activity = syncActivityRepository.findById(activityId).orElseThrow();
        String message = failure instanceof ApiException
                ? failure.getMessage()
                : "Synchronization failed because of an unexpected server error";
        activity.markFailed(Instant.now(), message);
    }

    private int totalReceived(SyncRecords records) {
        return records.users().size()
                + records.products().size()
                + records.batches().size()
                + records.sales().size()
                + records.saleItems().size()
                + records.appSettings().size();
    }

    private int sum(SyncResponse response, ToIntFunction<SyncEntityResult> value) {
        return value.applyAsInt(response.users())
                + value.applyAsInt(response.products())
                + value.applyAsInt(response.batches())
                + value.applyAsInt(response.sales())
                + value.applyAsInt(response.saleItems())
                + value.applyAsInt(response.appSettings());
    }

    private int applied(SyncEntityResult result) {
        return result.inserted() + result.updated();
    }
}
