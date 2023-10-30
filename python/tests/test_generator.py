from precisely import assert_that, equal_to, instance_of, raises

from abegidd.generator import split_edge_string, split_node_string


class TestSplitEdgeString:
    def test_simple_split(self):
        edge = "COMPOUND_treats_DISEASE"

        head, edge_type, tail = split_edge_string(edge)

        assert_that(head, equal_to("COMPOUND"))
        assert_that(edge_type, equal_to("treats"))
        assert_that(tail, equal_to("DISEASE"))

    def test_bad_split_fails(self):
        edge = "COMPOUND_DISEASE"

        assert_that(lambda: split_edge_string(edge), raises(instance_of(ValueError)))


class TestSplitNodeString:
    def test_simple_split(self):
        value = "paracetamol_COMPOUND"

        node, node_type = split_node_string(value)

        assert_that(node, equal_to("paracetamol"))
        assert_that(node_type, equal_to("COMPOUND"))

    def test_bad_split_fails(self):
        value = "COMPOUND"

        assert_that(lambda: split_node_string(value), raises(instance_of(ValueError)))
