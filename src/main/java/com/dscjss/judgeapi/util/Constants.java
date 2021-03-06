package com.dscjss.judgeapi.util;

public class Constants {

    public static final int JUDGE_ID_DEFAULT = 1;


    public static final int MASTER_JUDGE_ID_DEFAULT = 1;
    public static final int MASTER_JUDGE_ID_CODE_IN_LESS = 2;


    public static final String FETCH_TEST_DATA_URL = System.getenv("TEST_DATA_URL");


    public static final String AUTH_TOKEN = System.getenv("AUTH_TOKEN");

    public static final String FILE_NAME_COMPILE_ERROR = "compile_error.txt";
    public static final String FILE_NAME_STD_OUTPUT = "std_output.txt";
    public static final String FILE_NAME_STD_ERROR = "std_error.txt";
}
