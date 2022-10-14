package project1.src.main;

import project1.src.logic.Test;

public class Main {

    public static void m () {
        System.out.println("");
    }
    public static void main(String[] args) {
        Test test = new Test();
        test.initAttr3List(1, 2, 3);
        System.out.println(test);

        m();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}