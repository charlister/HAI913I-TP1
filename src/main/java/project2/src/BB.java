package project2.src;

class BB {
    public AA attb1;
    public CC attb2;

    public void mb1() {
        mb2();
    }

    public void mb2() {
        attb2.mc2();
        attb1.ma1();
    }

    public void mb3() {
        attb1.ma3();
    }
}