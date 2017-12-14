package formula;

import java.util.Collection;

public class FoodexElement {

	private Collection<AttributeElement> facetList;
	private String baseTerm;
	
	public FoodexElement(String baseTerm, Collection<AttributeElement> facetList) {
		this.baseTerm = baseTerm;
		this.facetList = facetList;
	}
	
	public String getBaseTerm() {
		return baseTerm;
	}
	
	public Collection<AttributeElement> getFacetList() {
		return facetList;
	}
	
	private String printFacets() {
		
		StringBuilder sb = new StringBuilder();
		
		int count = 0;
		for (AttributeElement facet : facetList) {
			
			if (count > 0) {
				sb.append("$");
			}
			
			sb.append(facet.getId()).append(".").append(facet.getValue());
			
			count++;
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return baseTerm + "#" + printFacets();
	}
}
