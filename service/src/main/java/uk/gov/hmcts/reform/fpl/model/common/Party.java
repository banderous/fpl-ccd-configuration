package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.util.Date;

@Data
@AllArgsConstructor
public class Party {
    public final String partyID;
    public final String partyType;
    public final String firstName;
    public final String lastName;
    public final Date dateOfBirth;
    public final Address address;
    public final EmailAddress email;
    public final TelephoneNumber telephoneNumber;
}

