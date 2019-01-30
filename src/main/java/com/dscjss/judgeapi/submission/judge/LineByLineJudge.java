package com.dscjss.judgeapi.submission.judge;

import com.dscjss.judgeapi.submission.model.TestCase;

import java.util.Scanner;

public class LineByLineJudge implements Judge {

    @Override
    public int judge(TestCase testCase, String output) {
        String actualOutput = testCase.getOutput();
        Scanner scannerA = new Scanner(actualOutput);
        Scanner scannerB = new Scanner(output);
        boolean correct = false;
        while (scannerA.hasNextLine() && scannerB.hasNextLine()) {
            String a = scannerA.nextLine();
            String b = scannerB.nextLine();
            correct = b.equals(a);
            if(!correct){
                return 0;
            }
        }
        if(correct && !scannerA.hasNextLine() && !scannerB.hasNextLine()) {
            return 1;
        }

        return 0;
    }
}
