package com.github.oliverlj.jsonapi.configuration.parameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * A request param bean which contains the filter parameters.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
@Getter
@Setter
public class FilterParameters {

    private Map<String, Map<FilterOperator, Set<String>>> filters = new HashMap<>();

}
