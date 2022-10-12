package project.src.main;

import project.src.logic.Test;

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