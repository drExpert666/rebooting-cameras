package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties
public class TrassirSession {
   private String sid;
   private Integer success;

   private String error_code;
}
