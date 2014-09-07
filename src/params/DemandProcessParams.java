package params;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
public class DemandProcessParams {

	@JsonProperty 
	private String name;
	
	@JsonProperty 
	public int demandBatchSize;

}


