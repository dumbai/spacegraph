package com.jujutsu.tsne.barneshut;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum BTreePrinterTest {
	;

	private static Node<Integer> test1() {
        Node<Integer> root = new Node<>(2);
        Node<Integer> n11 = new Node<>(7);
        Node<Integer> n12 = new Node<>(5);
        Node<Integer> n21 = new Node<>(2);
        Node<Integer> n22 = new Node<>(6);
        Node<Integer> n23 = new Node<>(3);
        Node<Integer> n24 = new Node<>(6);
        Node<Integer> n31 = new Node<>(5);
        Node<Integer> n32 = new Node<>(8);
        Node<Integer> n33 = new Node<>(4);
        Node<Integer> n34 = new Node<>(5);
        Node<Integer> n35 = new Node<>(8);
        Node<Integer> n36 = new Node<>(4);
        Node<Integer> n37 = new Node<>(5);
        Node<Integer> n38 = new Node<>(8);

        root.left = n11;
        root.right = n12;

        n11.left = n21;
        n11.right = n22;
        n12.left = n23;
        n12.right = n24;

        n21.left = n31;
        n21.right = n32;
        n22.left = n33;
        n22.right = n34;
        n23.left = n35;
        n23.right = n36;
        n24.left = n37;
        n24.right = n38;

        return root;
    }

    private static Node<Integer> test2() {
        Node<Integer> root = new Node<>(2);
        Node<Integer> n11 = new Node<>(7);
        Node<Integer> n12 = new Node<>(5);
        Node<Integer> n21 = new Node<>(2);
        Node<Integer> n22 = new Node<>(6);
        Node<Integer> n23 = new Node<>(9);
        Node<Integer> n31 = new Node<>(5);
        Node<Integer> n32 = new Node<>(8);
        Node<Integer> n33 = new Node<>(4);

        root.left = n11;
        root.right = n12;

        n11.left = n21;
        n11.right = n22;

        n12.right = n23;
        n22.left = n31;
        n22.right = n32;

        n23.left = n33;

        return root;
    }

    public static void main(String[] args) {

        BTreePrinter.printNode(test1());
        BTreePrinter.printNode(test2());

    }
}

class Node<T extends Comparable<?>> {
    Node<T> left;
    Node<T> right;
    final T data;

    Node(T data) {
        this.data = data;
    }
}

enum BTreePrinter {
	;

	public static <T extends Comparable<?>> void printNode(Node<T> root) {
        int maxLevel = BTreePrinter.maxLevel(root);

        printNodeInternal(Collections.singletonList(root), 1, maxLevel);
    }

    private static <T extends Comparable<?>> void printNodeInternal(List<Node<T>> nodes, int level, int maxLevel) {
        if (nodes.isEmpty() || BTreePrinter.isAllElementsNull(nodes))
            return;

        int floor = maxLevel - level;
        int endgeLines = (int) Math.pow(2, (Math.max(floor - 1, 0)));
        int firstSpaces = (int) Math.pow(2, (floor)) - 1;
        int betweenSpaces = (int) Math.pow(2, (floor + 1)) - 1;

        BTreePrinter.printWhitespaces(firstSpaces);

        List<Node<T>> newNodes = new ArrayList<>();
        for (Node<T> node : nodes) {
            if (node != null) {
                System.out.print(node.data);
                newNodes.add(node.left);
                newNodes.add(node.right);
            } else {
                newNodes.add(null);
                newNodes.add(null);
                System.out.print(" ");
            }

            BTreePrinter.printWhitespaces(betweenSpaces);
        }
        System.out.println();

        for (int i = 1; i <= endgeLines; i++) {
            for (Node<T> node : nodes) {
                BTreePrinter.printWhitespaces(firstSpaces - i);
                if (node == null) {
                    BTreePrinter.printWhitespaces(endgeLines + endgeLines + i + 1);
                    continue;
                }

                if (node.left != null)
                    System.out.print("/");
                else
                    BTreePrinter.printWhitespaces(1);

                BTreePrinter.printWhitespaces(i + i - 1);

                if (node.right != null)
                    System.out.print("\\");
                else
                    BTreePrinter.printWhitespaces(1);

                BTreePrinter.printWhitespaces(endgeLines + endgeLines - i);
            }

            System.out.println();
        }

        printNodeInternal(newNodes, level + 1, maxLevel);
    }

    private static void printWhitespaces(int count) {
        for (int i = 0; i < count; i++)
            System.out.print(" ");
    }

    private static <T extends Comparable<?>> int maxLevel(Node<T> node) {
        if (node == null)
            return 0;

        return Math.max(BTreePrinter.maxLevel(node.left), BTreePrinter.maxLevel(node.right)) + 1;
    }

    private static <T> boolean isAllElementsNull(Iterable<T> list) {
        for (Object object : list) {
            if (object != null)
                return false;
        }

        return true;
    }
}