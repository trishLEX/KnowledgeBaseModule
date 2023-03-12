package ru.fa;

import com.google.common.collect.Streams;
import org.apache.commons.compress.utils.Lists;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.FBRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFLanguages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class JenaTest {

    private static final String URI = "http://knbase.fa.ru#";
    private static final String CHILDREN = URI + "children";
    private static final String PARENT = URI + "parent";
    private static final String TYPE = URI + "type";
    private static final String LEVEL = URI + "level";

    private Model example;

    @BeforeEach
    void before() {
        example = ModelFactory.createDefaultModel();
    }

    @Test //677
    void test() {
        var bank = example.createResource(URI + "bank");
        var another = example.createResource(URI + "anotherBank");
        var childBank = example.createResource(URI + "childBank");
        var sber = example.createResource(URI + "Sber");
        var centerSber = example.createResource(URI + "CenterSber");
        var uralSber = example.createResource(URI + "UralSber");
        var siberiaSber = example.createResource(URI + "SiberiaSber");

        var type = example.createProperty(TYPE);

        bank.addProperty(type, "BANK");
        another.addProperty(type, "BANK");
        childBank.addProperty(type, "BANK");
        sber.addProperty(type, "BANK");
        centerSber.addProperty(type, "BANK");
        uralSber.addProperty(type, "BANK");
        siberiaSber.addProperty(type, "BANK");

        addChild(bank, another);
        addChild(bank, sber);
        addChild(bank, childBank);

        addChild(sber, centerSber);
        addChild(sber, uralSber);
        addChild(sber, siberiaSber);

        var card = example.createResource(URI + "card");
        var debit = example.createResource(URI + "debit");
        var credit = example.createResource(URI + "credit");
        var gold = example.createResource(URI + "gold");
        var plain = example.createResource(URI + "plain");

        card.addProperty(type, "CARD");
        debit.addProperty(type, "CARD");
        credit.addProperty(type, "CARD");
        gold.addProperty(type, "CARD");
        plain.addProperty(type, "CARD");

        addChild(card, debit);
        addChild(card, credit);

        addChild(debit, gold);
        addChild(debit, plain);

        var rule1 = example.createResource(URI + "rule1");
        var rule2 = example.createResource(URI + "rule2");
        var rule3 = example.createResource(URI + "rule3");
        var rule4 = example.createResource(URI + "rule4");
        var dimValueProperty = example.createProperty(URI + "dimValue");
        rule1.addProperty(dimValueProperty, bank);
        rule2.addProperty(dimValueProperty, sber);
        rule3.addProperty(dimValueProperty, siberiaSber);
        rule4.addProperty(dimValueProperty, bank);

        var dimCardProperty = example.createProperty(URI + "cardValue");
        rule1.addProperty(dimCardProperty, credit);
        rule2.addProperty(dimCardProperty, debit);
        rule3.addProperty(dimCardProperty, plain);
        rule4.addProperty(dimCardProperty, card);

        example.write(System.out, RDFLanguages.TTL.getName(), URI);

        String rules = "[r1: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#dimValue ?bc) " +
                "            (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#cardValue ?cc) " +
                "-> (?o http://knbase.fa.ru#dimValue ?bc)" +
                "   (?o http://knbase.fa.ru#cardValue ?cc)" +
                "]" +
                "[r2: (?o http://knbase.fa.ru#dimValue  ?b) noValue(?b http://knbase.fa.ru#children ?bc) " +
                "     (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#cardValue ?cc) " +
                "-> (?o http://knbase.fa.ru#cardValue ?cc)]" +
                "[r3: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#dimValue ?bc) " +
                "     (?o http://knbase.fa.ru#cardValue ?c) noValue(?c http://knbase.fa.ru#children ?cc) " +
                "-> (?o http://knbase.fa.ru#dimValue ?bc)]";

//        String rules = "[r1: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) " +
//                "            (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) " +
//                "-> (?o http://knbase.fa.ru#dimValue ?bc)" +
//                "   (?o http://knbase.fa.ru#cardValue ?cc)" +
//                "]";

//        String rules = "[r1: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#dimValue ?bc)" +
//                "            (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) -> (?o http://knbase.fa.ru#dimValue ?bc) (?o http://knbase.fa.ru#cardValue ?cc)] " +
////                "       [r2: (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#cardValue ?cc)" +
////                "            (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) -> (?o http://knbase.fa.ru#cardValue ?cc) (?o http://knbase.fa.ru#dimValue ?bc)]" +
//                "";

//        String rules = "[r1: (?b http://knbase.fa.ru#type 'BANK') (?c http://knbase.fa.ru#type 'CARD') " +
//                "(?b http://knbase.fa.ru#parent ?bp) (?c http://knbase.fa.ru#parent ?cp) " +
//                "(?o http://knbase.fa.ru#dimValue ?b) noValue(?o http://knbase.fa.ru#cardValue ?c) " +
//                "(?o1 http://knbase.fa.ru#dimValue ?bp) (?o1 http://knbase.fa.ru#cardValue ?cp) -> (?o1 http://knbase.fa.ru#dimValue ?b) (?o1 http://knbase.fa.ru#cardValue ?c)]" +
//                "" +
//                "[r2: (?b http://knbase.fa.ru#type 'BANK') (?c http://knbase.fa.ru#type 'CARD') " +
//                "(?b http://knbase.fa.ru#parent ?bp) (?c http://knbase.fa.ru#parent ?cp) " +
//                "noValue(?o http://knbase.fa.ru#dimValue ?b) (?o http://knbase.fa.ru#cardValue ?c) " +
//                "(?o1 http://knbase.fa.ru#dimValue ?bp) (?o1 http://knbase.fa.ru#cardValue ?cp) -> (?o1 http://knbase.fa.ru#dimValue ?b) (?o1 http://knbase.fa.ru#cardValue ?c)]" +
//                "" +
//                "[r3: (?b http://knbase.fa.ru#type 'BANK') (?c http://knbase.fa.ru#type 'CARD') " +
//                "(?b http://knbase.fa.ru#parent ?bp) (?c http://knbase.fa.ru#parent ?cp) " +
//                "noValue(?o http://knbase.fa.ru#dimValue ?b) noValue(?o http://knbase.fa.ru#cardValue ?c) " +
//                "(?o1 http://knbase.fa.ru#dimValue ?bp) (?o1 http://knbase.fa.ru#cardValue ?cp) -> (?o1 http://knbase.fa.ru#dimValue ?b) (?o1 http://knbase.fa.ru#cardValue ?c)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Reasoner genericRulesReasoner = new GenericRuleReasoner(ruleList);
        InfModel rulesModel = ModelFactory.createInfModel(genericRulesReasoner, example);

        System.out.println("Statements");
        var rulesList = rulesModel.listStatements();
        while (rulesList.hasNext()) {
            System.out.println(rulesList.next());
        }

        System.out.println("Get property rule1");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule1.getURI()).listProperties()));

        System.out.println("Get property rule2");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule2.getURI()).listProperties()));

        System.out.println("Get property rule3");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule3.getURI()).listProperties()));

        System.out.println("Get property rule4");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule4.getURI()).listProperties()));
    }

    // пример из фото телеграма
    @Test
    void test2() {
        var childrenProperty = example.createProperty(CHILDREN);
        var firstValueProperty = example.createProperty(URI + "firstValue");
        var secondValueProperty = example.createProperty(URI + "secondValue");

        var firstRootProperty = example.createProperty(URI + "firstRoot");
        var secondRootProperty = example.createProperty(URI + "secondRoot");

        var nilrule = example.createResource(URI + "nilrule");
        var nilNode = createNode("nil", -1, nilrule, firstValueProperty);
        var nilNode2 = createNode("nil2", -1, nilrule, secondValueProperty);
        nilrule.addProperty(firstValueProperty, nilNode);
        nilrule.addProperty(firstRootProperty, nilNode);
        nilrule.addProperty(secondValueProperty, nilNode2);
        nilrule.addProperty(secondRootProperty, nilNode2);

        var node5 = createNode("5", 2, nilrule, firstValueProperty);
        var node11 = createNode("11", 3, nilrule, firstValueProperty);
        var node12 = createNode("12", 3, nilrule, firstValueProperty);
        var node3 = createNode("3", 1, nilrule, firstValueProperty);
        var node4 = createNode("4", 2, nilrule, firstValueProperty);
        var node1 = createNode("1", 0, nilrule, firstValueProperty);
        var node2 = createNode("2", 1, nilrule, firstValueProperty);

        addChild(node5, node12);
        addChild(node5, node11);
        addChild(node1, node3);
        addChild(node2, node4);
        addChild(node1, node2);
        addChild(node2, node5);

        var node8 = createNode("8", 1, nilrule, secondValueProperty);
        var node9 = createNode("9", 2, nilrule, secondValueProperty);
        var node6 = createNode("6", 0, nilrule, secondValueProperty);
        var node10 = createNode("10", 2, nilrule, secondValueProperty);
        var node7 = createNode("7", 1, nilrule, secondValueProperty);
        var node13 = createNode("13", 3, nilrule, secondValueProperty);
        var node14 = createNode("14",3, nilrule, secondValueProperty);

        addChild(node7, node10);
        addChild(node6, node7);
        addChild(node7, node9);
        addChild(node6, node8);
        addChild(node10, node13);
        addChild(node10, node14);

        var rule3 = example.createResource(URI + "rule3");
        var rule2 = example.createResource(URI + "rule2");
        var rule1 = example.createResource(URI + "rule1");
        var rule4 = example.createResource(URI + "rule4");
        var rule5 = example.createResource(URI + "rule5");

        rule1.addProperty(firstValueProperty, node2);
        rule1.addProperty(secondValueProperty, node10);
        rule1.addProperty(firstRootProperty, node2);
        rule1.addProperty(secondRootProperty, node10);

        rule2.addProperty(firstValueProperty, node4);
        rule2.addProperty(secondValueProperty, node9);
        rule2.addProperty(firstRootProperty, node4);
        rule2.addProperty(secondRootProperty, node9);

        rule3.addProperty(firstValueProperty, node1);
        rule3.addProperty(secondValueProperty, node6);
        rule3.addProperty(firstRootProperty, node1);
        rule3.addProperty(secondRootProperty, node6);

        rule4.addProperty(firstValueProperty, node5);
        rule4.addProperty(secondValueProperty, node13);
        rule4.addProperty(firstRootProperty, node5);
        rule4.addProperty(secondRootProperty, node13);

        rule5.addProperty(firstValueProperty, node5);
        rule5.addProperty(secondValueProperty, node14);
        rule5.addProperty(firstRootProperty, node5);
        rule5.addProperty(secondRootProperty, node14);

        String rules = "" +
                "[r1: (?o http://knbase.fa.ru#firstValue  ?b)  (?b http://knbase.fa.ru#children ?bc) " +
                "        ->  (?o http://knbase.fa.ru#firstValue ?bc) ]" +
                "[r2: (?o http://knbase.fa.ru#secondValue  ?c) (?c http://knbase.fa.ru#children ?cc)" +
                "     -> (?o http://knbase.fa.ru#secondValue ?cc) ]" +
                "[r3: (?o http://knbase.fa.ru#firstValue ?b) (?b http://knbase.fa.ru#children ?bc) notEqual(?o1, ?o) (?o1 http://knbase.fa.ru#firstValue ?bc) (?o http://knbase.fa.ru#firstRoot ?or) (?o1 http://knbase.fa.ru#firstRoot ?o1r) (?or http://knbase.fa.ru#level ?orl) (?o1r http://knbase.fa.ru#level ?o1rl) greaterThan(?orl ?o1rl)" +
                "        -> print(?o, ?b, ?bc, ?o1, ?or, ?o1r, ?orl, ?o1rl) remove(3) ]" +
                "[r4: (?o http://knbase.fa.ru#secondValue  ?c) (?c http://knbase.fa.ru#children ?cc) notEqual(?o1, ?o) (?o1 http://knbase.fa.ru#secondValue ?cc) (?o http://knbase.fa.ru#secondRoot ?or) (?o1 http://knbase.fa.ru#secondRoot ?o1r) (?or http://knbase.fa.ru#level ?orl) (?o1r http://knbase.fa.ru#level ?o1rl) greaterThan(?orl ?o1rl) " +
                "     -> print(?o, ?c, ?cc, ?o1, ?or, ?o1r, ?orl, ?o1rl) remove(3) ]";

        List<Rule> ruleList = Rule.parseRules(rules);
        FBRuleReasoner genericRulesReasoner = new GenericRuleReasoner(ruleList);
//        genericRulesReasoner.setTraceOn(true);
        InfModel rulesModel = ModelFactory.createInfModel(genericRulesReasoner, example);

        System.out.println("Statements");
        var rulesList = rulesModel.listStatements();
        while (rulesList.hasNext()) {
            System.out.println(rulesList.next());
        }

        System.out.println("Get property rule1");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule1.getURI()).listProperties()));

        System.out.println("Get property rule2");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule2.getURI()).listProperties()));

        System.out.println("Get property rule3");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule3.getURI()).listProperties()));

        System.out.println("Get property rule4");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule4.getURI()).listProperties()));

        System.out.println("Get property rule5");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule5.getURI()).listProperties()));

        System.out.println("Get observation rule");
        //todo можно сравнить как быстро АПИ и SPARQL отрабатывают
        System.out.println(Streams.stream(rulesModel.listResourcesWithProperty(firstValueProperty, node12))
                .filter(res -> res.getProperty(secondValueProperty).getResource().equals(node13))
                .collect(Collectors.toList()));

        var strQuery = "" +
                "prefix kn: <http://knbase.fa.ru#> " +
                "select ?r " +
                "where {" +
                "   ?r kn:firstValue kn:12 . " +
                "   ?r kn:secondValue kn:13 . " +
                "}";
        Query query = QueryFactory.create(strQuery);
        try (var qexec = QueryExecutionFactory.create(query, rulesModel)) {
            var rs = qexec.execSelect();
            System.out.println(rs.next().getResource("?r"));
        }
    }

    private void addChild(Resource parent, Resource child) {
        var childrenProperty = example.createProperty(CHILDREN);
        var parentProperty = example.createProperty(PARENT);
        parent.addProperty(childrenProperty, child);
        child.addProperty(parentProperty, parent);
    }

    private Resource createNode(String index, int level, Resource nilrule, Property valueProperty) {
        var node = example.createResource(URI + index);
        var levelProperty = example.createProperty(LEVEL);
        node.addProperty(levelProperty, Integer.toString(level), XSDDatatype.XSDinteger);
        var childrenProperty = example.createProperty(CHILDREN);
        node.addProperty(childrenProperty, node);
        nilrule.addProperty(valueProperty, node);
        return node;
    }

    private Resource createNode(String index) {
        var node = example.createResource(URI + index);
//        var childrenProperty = example.createProperty(CHILDREN);
//        node.addProperty(childrenProperty, node);
        return node;
    }
}
