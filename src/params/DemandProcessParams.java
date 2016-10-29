package params;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
public class DemandProcessParams extends AbstractParams {

    public static DemandProcessParams DEFAULT = new DemandProcessParams("ContinuousDemandProcess", 0);
    
	@JsonProperty 
	protected String name;
	
	@JsonProperty 
	protected int demandBatchSize;

}


