import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Prichozi hovor do call centra
 */
class IncomingCall {
    /** valajici cislo */
    int callingNumber;
    /** Cas kdy hovor prisel (v sekundach od zacaku smeny) */
    int time;
}

/**
 * Dostupny operator
 * @author Marek Siřiště
 */
class FreeOperator{
    /** jmeno operatora */
    String name;
    /** Cas kdy se operator prihlasil jako dostupny (v sekundach od zacaku smeny) */
    int time;

    public FreeOperator(String name, int time){
        this.name=name;
        this.time=time;
    }
}

public class CallDispatching {
    public static void main(String[] args) throws IOException {
        DispatcherGenerickaFronta d1=new DispatcherGenerickaFronta();
        d1.Cteni();
        d1.ZapisDoSouboru("dispatching.txt");
    }
}


class Link_T {
    /** Data prvku - libovolny objekt*/
    Object data;
    /** Dalsi prvek spojoveho seznamu */
    Link_T next;
}

/**
 * Genericka fronta
 * @author Marek Siřiště
 */
class Queue {
    /** Prvni prvek fronty */
    public Link_T first;
    /** Posledni prvek fronty */
    public Link_T last;

    /**
     * Prida prichozi hovor na konec fronty
     * @param object - libovolny objekt pridany do fronty
     */
    public void add(Object object) {
        Link_T nl = new Link_T();
        nl.data = object;
        if (first == null) {
            first = nl;
            last = nl;
        }
        else {
            last.next = nl;
            last=nl;
        }
    }

    /**
     * Vrati prvni objekt ve fronte
     * @return prvni objekt nebo null, pokud je fornta prazdna
     */
    public Object get() {
        if (first != null) {
            return first.data;
        }
        else {
            return null;
        }
    }

    /**
     * Odstrani prvni objekt z fronty, pokud fronta neni prazdna
     */
    public void removeFirst() {
        if (first != null) {
            first = first.next;
        }
        else {
            System.out.println("Remove object on empty queue. Probably error, continuing...");
        }
    }
    /**
     * Zjistí, zda fronta je prázdná
     * @return true/ false
     */
    public boolean IsEmpty(){
        if(first==null) return true;
        return false;
    }
}
class DispatcherGenerickaFronta {
    /** Genericka fronta pro hovory */
    private Queue queue_calls;
    /** Genericka fronta pro operatory*/
    private Queue queue_operators;
    /** fronta pro vypis do textoveho souboru */
    private Queue stringQueue;
    /** nejdele cekajici operator */
    private FreeOperator operator_nejdelsi;
    /** prirazeny hovor k nejdele cekajicimu operatorovi */
    private IncomingCall hovor_nejdelsi;
    /** nejdelsi cas pauzy */
    private int pause_time;

    /**
     * Vytvori novou instanci s prazdnymi frontami
     */
    public DispatcherGenerickaFronta() {
        this.queue_calls = new Queue();
        this.queue_operators=new Queue();
    }

    /**
     * Zaradi prichozi hovor do fronty
     * @param number telefonni cislo prichoziho hvoru
     * @param time cas zacatku hovoru (v sekundach od zacatku smeny)
     */
    public void call(int number, int time) {
        IncomingCall call = new IncomingCall();
        call.callingNumber = number;
        call.time = time;
        queue_calls.add(call);
    }

    /**
     * Zaradi volneho operatora do fronty
     * @param name jmeno volneho operatora
     * @param time cas zarazeni volneho operatora do fronty (v sekundach od zacatku smeny)
     */
    public void freeOperator(String name, int time) {
        queue_operators.add(new FreeOperator(name, time)); // operator name se time sekund od zacatku smeny prihlasil jako dostupny
    }

    /**
     * Vytvori novou frontu a tim tu puvodni smaze (kdyby chtel uzivatel vypisovat az od urciteho data)
     */
    public void SetStringQueue (){
        this.stringQueue=new Queue();
    }

    /**
     * Priradi nejdele cekajici hovor z fronty nejdele cekajicimu operatorovi z fronty
     */
    public void dispatchCall() throws IOException {
        if(!(queue_calls.IsEmpty())&&!(queue_operators.IsEmpty())){
            IncomingCall call = (IncomingCall) queue_calls.get();
            FreeOperator operator = (FreeOperator) queue_operators.get();
            int cas= call.time-operator.time;
            if (pause_time < cas) {
                    pause_time = cas;
                    hovor_nejdelsi = call;
                    operator_nejdelsi = operator;
            }
            queue_calls.removeFirst();
            queue_operators.removeFirst();
            assignCall(call, operator);

        }
        else {
            stringQueue.add("Momentalne nelze spojit.");
            System.out.println("Momentalne nelze spojit.");
        }
    }

    /**
     * Priradi zadany prichozi hovor zadanemu volnemu operatorovi
     * @param call prichozi hovor
     * @param operator volny operator
     */
    private void assignCall(IncomingCall call, FreeOperator operator) throws IOException {
        String radek1=operator.name + " is answering call from +420 " + call.callingNumber;
        stringQueue.add(radek1);
        String radek2="The caller has waited for " + Math.max(0, operator.time - call.time) + " seconds.";
        stringQueue.add(radek2);
        System.out.println(radek1+"\n"+radek2);
    }

    public void Cteni() throws IOException {
        String radek="";
        SetStringQueue();//Vytvori novou frontu - vystup pro kazdy soubor, ktery nacteme bude mít svuj vystup zvlast
        try(Scanner vstup=new Scanner(Paths.get("callCentrum.txt"))){
            while(vstup.hasNextLine()&&!(vstup.equals(""))){
                radek=vstup.nextLine();
                String[] parametry=radek.split(" ");
                if(parametry[0].equalsIgnoreCase("c")){
                    call(Integer.parseInt(parametry[2]),Integer.parseInt(parametry[1]));
                }
                else if(parametry[0].equalsIgnoreCase("o")){
                    freeOperator(parametry[2],Integer.parseInt(parametry[1]));
                }
                dispatchCall();
            }
        }
        System.out.println("Nejdelsi pauzu mel(a) operator: "+operator_nejdelsi.name +". Konkretne cekal(a): "+pause_time+" s. Nakonec byl spojen s cislem: +420"+hovor_nejdelsi.callingNumber+".");
    }
    /**
     * Zapise vystup do souboru
     * @param nazev nazev souboru, kam budeme zapisovat
     */
    public void ZapisDoSouboru(String nazev){
        try {
            FileWriter writer = new FileWriter(nazev);
            while(!(stringQueue.IsEmpty())){
                writer.write(stringQueue.get()+"\n");
                stringQueue.removeFirst();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
