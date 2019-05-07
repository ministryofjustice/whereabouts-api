package uk.gov.justice.digital.hmpps.whereabouts.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "ABSENT_REASONS")
@Data()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsentReason {

    @Id()
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private String reason;

    private String pnomisCode;

    private boolean paidReason;
}
