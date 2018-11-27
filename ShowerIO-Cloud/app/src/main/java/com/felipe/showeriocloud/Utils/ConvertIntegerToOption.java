package com.felipe.showeriocloud.Utils;

import java.util.Map;

public interface ConvertIntegerToOption {

    Map<String,Integer> fillPositionsMap();

    int convert(int time);
}
