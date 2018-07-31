package com.etatransit.fragment;

/**
 * Created by Innovate on 6/24/2017.
 */
public class FindFragment {
    public static String fragmentName;
    public static void setFragment(String frag)
    {
        fragmentName = frag;
//        Log.d("onCreate", "setFragment: "+fragmentName);
    }
    public static String getFragment()
    {
        return fragmentName;
    }

}
