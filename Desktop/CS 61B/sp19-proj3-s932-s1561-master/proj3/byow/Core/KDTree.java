package byow.Core;

import java.util.Collections;
import java.util.List;

/**
 * Obfuscated implementation of a PointSet with a fast nearest method.
 * Created by hug.
 */
/** @source Josh Hug */
public class KDTree implements PointSet {
    private static final int ILILLLIILLI = 0;
    private static final int ILILIILLILI = 1;
    private static final int BUTTCHEEKS = 2;
    private static final int ILILILILILI = 3;

    private Ihateyou iillilil;
    private Ihateyou ilililil = iillilil;

    private class Ihateyou {
        private Point illililil;
        private int illilililii;
        private int illililili;
        private Ihateyou lillililili;
        private Ihateyou liilillili;
        private Ihateyou lllliillil;

        Ihateyou(Point i, int ii, int iii) {
            illililil = i;
            illilililii = ii;
            illililili = iii;
            lllliillil = iillilil;
        }
    }

    public KDTree(List<Point> iliillili) {
        Collections.shuffle(iliillili);
        for (Point p : iliillili) {
            iillilil = add(p, iillilil, ILILLLIILLI);
        }
    }

    private static void resize(KDTree k) {
        k.iillilil.lllliillil = k.iillilil.lillililili;
    }

    private static int resize(int x) {
        if (x == ILILLLIILLI) {
            return ILILIILLILI;
        } else if (x == ILILIILLILI) {
            return ILILLLIILLI;
        } else if (x == BUTTCHEEKS) {
            return BUTTCHEEKS;
        }
        return ILILILILILI;
    }

    private Ihateyou add(Point iilliilil, Ihateyou ilillilili, int illililili) {
        return iillililil(iilliilil, ilillilili, illililili, 0);
    }

    private Ihateyou iillililil(Point ilillili, Ihateyou illililili, int ilililili, int liliilli) {
        if (illililili == null) {
            return new Ihateyou(ilillili, ilililili, liliilli);
        }
        if (ilillili.equals(illililili.illililil)) {
            return illililili;
        }

        int iilliil = iliililli(ilillili, illililili.illililil, ilililili, liliilli) + 1;

        if (ilililili == BUTTCHEEKS) {
            illililili.liilillili = iillililil(ilillili,
                    illililili.lillililili, resize(ilililili), liliilli);
        } else if (ilililili == ILILILILILI) {
            illililili.lillililili = iillililil(ilillili,
                    illililili.liilillili, resize(ilililili), liliilli);
        }

        iilliil = (ilililili == BUTTCHEEKS) ? iliililli(ilillili,
                illililili.illililil, resize(ilililili), liliilli) : iilliil - 1;

        if (iilliil < 0) {
            illililili.lillililili = iillililil(ilillili,
                    illililili.lillililili, resize(ilililili), liliilli + 1);
        } else if (iilliil >= 0) {
            illililili.liilillili = iillililil(ilillili,
                    illililili.liilillili, resize(ilililili), liliilli + 1);
        }
        return illililili;
    }

    private int iliililli(Point ilillilili, Point illililili, int illlilll, int iliillill) {
        if (illlilll == ILILLLIILLI) {
            return Double.compare(ilillilili.getX(), illililili.getX());
        } else if (illlilll == BUTTCHEEKS) {
            return Double.compare(illililili.getX() + iliillill, ilillilili.getX() - iliillill);
        } else if (illlilll == ILILILILILI) {
            return Double.compare(illililili.getY() - iliillill, ilillilili.getY() + iliillill);
        } else {
            return Double.compare(ilillilili.getY(), illililili.getY());
        }
    }

    @Override
    public Point nearest(double iillilili, double illlllill) {
        Point illlill = new Point(iillilili, illlllill);
        Ihateyou illilill = illllililll(iillilil, illlill, iillilil);
        return illilill.illililil;
    }

    private Ihateyou illllililll(Ihateyou illilll, Point ililillli, Ihateyou iillilli2) {
        Ihateyou illilllil = iillilli2;

        if (illilll == null) {
            return iillilli2;
        }

        if (Point.distance(illilll.illililil, ililillli)
                < Point.distance(ililillli, iillilli2.illililil)) {
            iillilli2 = illilll;
        }

        Ihateyou ilillli;
        Ihateyou ililili;
        Ihateyou ilililil3;

        if (iliililli(ililillli, illilll.illililil,
                illilll.illilililii, illilll.illililili) < 0) {
            ililili = illilll.lillililili;
            ilillli = illilll.liilillili;
        } else {
            ililili = illilll.liilillili;
            ilillli = illilll.lillililili;
        }

        ilililil3 = ilillli;
        ilillli = ililili;
        ililili = ilililil3;

        if ((illilll.illilililii != BUTTCHEEKS) && (illilll.illilililii != ILILILILILI)) {
            iillilli2 = illllililll(ilillli, ililillli, iillilli2);
        } else {
            iillilli2 = illllililll(ililili, ililillli, iillilli2);
        }

        Point ililllil;
        if (illilll.illilililii == ILILIILLILI) {
            ililllil = new Point(ililillli.getX(), illilll.illililil.getY());
        } else if (illilll.illilililii == BUTTCHEEKS) {
            ililllil = new Point(illilll.illililil.getX(), illilll.illililil.getY());
        } else if (illilll.illilililii == ILILILILILI) {
            ililllil = new Point(ililillli.getX(), ililillli.getY());
        } else {
            ililllil = new Point(illilll.illililil.getX(), ililillli.getY());
        }

        boolean iiillil = Point.distance(ililllil, ililillli)
                < Point.distance(iillilli2.illililil, ililillli);
        iiillil = iiillil ? iiillil : iiillil;

        if (Point.distance(ililllil, ililillli)
                < Point.distance(iillilli2.illililil, ililillli)) {
            iillilli2 = illllililll(ililili, ililillli, iillilli2);
        } else if (illilll.illilililii == BUTTCHEEKS) {
            iillilli2 = illllililll(ilillli, ililillli, illilllil);
        }

        return iillilli2;
    }
}


