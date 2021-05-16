package huji.postpc.y2021.multicalculator;

import java.io.Serializable;

public class Calculation implements Serializable, Comparable<Calculation> {
    double root;
    double curCandidate;
    double div1;
    double div2;
    boolean inProgress;
    boolean isPrime;
    int id;
    int progress;
    String workId;
    static int items_created = 0;

    Calculation(){
        this.curCandidate = 2;
        this.id = items_created;
        this.inProgress = true;
        items_created += 1;
        progress = 0;
        isPrime = false;

    }

    Calculation(double root){
        this.curCandidate = 2;
        this.inProgress = true;
        this.root = root;
        this.id = items_created;
        this.workId = null;
        this.progress = 0;
        items_created += 1;
        isPrime = false;
        div1 = -1;
        div2 = -1;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    @Override
    public String toString() {
        if (this.inProgress){
            return String.valueOf((int) root);
        }
         if (this.isPrime){
             return (int) root + " is prime!";
         }

         else {
             return (int) root + " roots are: " + (int)div1 + ", " + (int)div2;
         }
    }

    @Override
    public int compareTo(Calculation calculation) {
        if (this.inProgress && !calculation.inProgress){
            return 1;
        }
        else if(!this.inProgress && calculation.inProgress){
            return -1;
        }

        else if (this.id < calculation.id){
            return 1;
        }

        return -1;
    }
}


