package com.QMe2.SMSClasses;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDetails {
	   private List<String> numbers;
	   private String message;
}
