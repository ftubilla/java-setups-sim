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
public class DemandProcessParams extends AbstractParams {

	@JsonProperty 
	protected String name;
	
	@JsonProperty 
	protected int demandBatchSize;

}


