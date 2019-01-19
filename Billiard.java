import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Billiard implements ActionListener {
    JFrame jfrm;
    BilliardGraphics bg;

    // default constructor
    Billiard() {
        jfrm = new JFrame("Billiard");
        bg = new BilliardGraphics();

        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jfrm.add(bg,BorderLayout.CENTER);
        jfrm.pack();
        jfrm.setResizable(false);
        jfrm.setVisible(true);

        int delay = 100;
        Timer timer = new Timer(delay,this);
        while(true) {
            timer.start();
        }
    }

    public void actionPerformed(ActionEvent e) {
        bg.repaint();
    }

    // main
    public static void main(String args[]) {
        new Billiard();
    }
}


class BilliardGraphics extends JComponent {
    int WIDTH = 700;
    int HEIGHT = WIDTH;

    double LAMBDA = 1;

    // MULTIPLIER determines how much of the window should be filled with
    // the bounding disks.
    double MULTIPLIER = 1.01; // Should be slightly larger than 1

    double DELTAX = 0.001; // finite difference in space for computing force

    double BIGH = 0.1; // large timestep
    double SMALLH = 0.0001; // small timestep

    double oval_width = WIDTH*MULTIPLIER;
    double oval_radius = oval_width*0.5;

    double DELTAT = 0.5; // timestep for display purposes

    double h = BIGH;
    double[] state = new double[] {0.5*WIDTH,0.5*HEIGHT,-3,-10};

    double[] historyX = new double[] {state[0]};
    double[] historyY = new double[] {state[1]};

    BilliardGraphics() {
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw the bounding disks
        g.fillOval((int) (-0.5*oval_width),(int) (-0.5*oval_width),
            (int) oval_width,(int) oval_width);
        g.fillOval((int) (WIDTH-0.5*oval_width),(int) (-0.5*oval_width),
            (int) oval_width,(int) oval_width);
        g.fillOval((int) (-0.5*oval_width),(int) (HEIGHT-0.5*oval_width),
            (int) oval_width,(int) oval_width);
        g.fillOval((int) (WIDTH-0.5*oval_width),(int) (HEIGHT-0.5*oval_width),
            (int) oval_width,(int) oval_width);

        // time-stepping from t to t+h
        double t = 0;
        while(t < DELTAT) {
            double[] newstate = update(state[0],state[1],state[2],state[3],h);
            if(depth(newstate[0],newstate[1]) < 0.1) {
                h = BIGH;
            } else {
                h = SMALLH;
                newstate = update(state[0],state[1],state[2],state[3],h);
            }
            state = newstate;
            t += h;
        }

        // save the past
        double[] newHistoryX = new double[historyX.length+1];
        double[] newHistoryY = new double[historyY.length+1];
        for(int i=0; i<historyX.length; i++) {
            newHistoryX[i] = historyX[i];
            newHistoryY[i] = historyY[i];
            g.fillOval((int) (historyX[i]-1), (int) (historyY[i]-1), 2, 2);
        }
        newHistoryX[historyX.length] = state[0];
        newHistoryY[historyY.length] = state[1];
        historyX = newHistoryX;
        historyY = newHistoryY;

        // draw the present state
        g.fillOval((int) (state[0]-6),(int) (state[1]-6), 12, 12);
    }


    // returns 0 if inside the free region, otherwise return the
    // depth into the forbidden region
    public double depth(double x, double y) {
        double depth_ul = Math.pow(oval_radius,2) - (Math.pow(x,2)+Math.pow(y,2));
        double depth_ur = Math.pow(oval_radius,2) - (Math.pow(x-WIDTH,2)+Math.pow(y,2));
        double depth_ll = Math.pow(oval_radius,2) - (Math.pow(x,2)+Math.pow(y-HEIGHT,2));
        double depth_lr = Math.pow(oval_radius,2) - (Math.pow(x-WIDTH,2)+Math.pow(y-HEIGHT,2));
        
        if(depth_ul<0 && depth_ur<0 && depth_ll<0 && depth_lr<0) {
            return 0;
        }

        double depth0 = Math.max(depth_ul,0) + Math.max(depth_ur,0) +
              Math.max(depth_ll,0) + Math.max(depth_lr,0);

        return LAMBDA*depth0;
    }

    // return the x-acceleration vector at a point (x,y) ... this will 
    // be zero if depth = 0
    public double vdotx(double x,double y) {
        if(depth(x,y)<0.1) {
            return 0;
        }

        return -0.5*(depth(x+DELTAX,y)-depth(x-DELTAX,y))/DELTAX;
    }

    // return the y-acceleration vector at a point (x,y) ... this will 
    // be zero if depth = 0
    public double vdoty(double x,double y) {
        if(depth(x,y)<0.1) {
            return 0;
        }

        return -0.5*(depth(x,y+DELTAX)-depth(x,y-DELTAX))/DELTAX;
    }

    // update the current state
    public double[] update(double x,double y,double vx,double vy, double h) {
        double[] k1 = new double[] {h*vx,h*vy,h*vdotx(x,y),h*vdoty(x,y)};

        double[] k2 = new double[] {h*(vx+0.5*k1[2]), h*(vy+0.5*k1[3]),
                 h*vdotx(x+0.5*k1[0],y+0.5*k1[1]), h*vdoty(x+0.5*k1[0],y+0.5*k1[1])};

        return new double[] {x+k2[0],y+k2[1],vx+k2[2],vy+k2[3]};
    }
}

