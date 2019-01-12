package com.dscjss.judgeapi.submission.judge;

import com.dscjss.judgeapi.submission.model.TestCase;

import java.util.Scanner;

public class LineByLineJudge implements Judge {

    @Override
    public int judge(TestCase testCase, String output) {
        String actualOutput = testCase.getOutput();
        Scanner scannerA = new Scanner(actualOutput);
        Scanner scannerB = new Scanner(output);
        while (scannerA.hasNextLine()) {
            String a = scannerA.nextLine();
            String b = scannerB.nextLine();
            if (b == null || !b.equals(a)) {
                return 0;
            }
        }
        return 1;
    }
}
