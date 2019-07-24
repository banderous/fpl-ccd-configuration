package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class ChildrenMigrationService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("children1") || !caseDetails.getData().containsKey("children")) {
            data.put("childrenMigrated", "Yes");

            if (!caseDetails.getData().containsKey("children1")) {
                List<Map<String, Object>> populatedChild = new ArrayList<>();
                // Populate partyId to satisfy data requirements of reform. Field is not to be shown in UI
                populatedChild.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", ImmutableMap.of(
                        "party", ImmutableMap.of(
                            "partyId", UUID.randomUUID().toString()
                        )
                    )
                ));

                data.put("children1", populatedChild);
            }
        } else {
            data.put("childrenMigrated", "No");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse addHiddenValues(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("children1")) {
            List<Map<String, Object>> childrenParties = (List<Map<String, Object>>) data.get("children1");

            List<ChildParty> childrenPartyList = childrenParties.stream()
                .map(entry -> mapper.convertValue(entry.get("value"), Map.class))
                .map(map -> mapper.convertValue(map.get("party"), ChildParty.class))
                .map(child -> {
                    ChildParty.ChildPartyBuilder partyBuilder = child.toBuilder();

                    if (child.getPartyId() == null) {
                        partyBuilder.partyId(UUID.randomUUID().toString());
                        partyBuilder.partyType(PartyType.INDIVIDUAL);
                    }

                    return partyBuilder.build();
                })
                .collect(toList());

            List<Map<String, Object>> children = childrenPartyList.stream()
                .map(item -> ImmutableMap.<String, Object>builder()
                    .put("id", UUID.randomUUID().toString())
                    .put("value", ImmutableMap.of(
                        "party", mapper.convertValue(item, Map.class)))
                    .build())
                .collect(toList());

            data.put("children1", children);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}