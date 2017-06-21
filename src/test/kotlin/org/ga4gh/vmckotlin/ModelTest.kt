package org.ga4gh.vmckotlin

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * @author Jules Jacobsen <j.jacobsen></j.jacobsen>@qmul.ac.uk>
 */
internal class ModelTest {

    val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    val prettyMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .writerWithDefaultPrettyPrinter()

    /**
     * In a real working code situation this will be doing a lookup against the biocommons.seqrepo
     * https://github.com/biocommons/biocommons.seqrepo to get the sequence hash.
     */
    val chr19GRCh38Identifier = Identifier(namespace = "NCBI", accession = "NC_000019.10")
    val chr19GRCh38SequenceIdentifier = "VMC:GS_IIB53T8CNeJJdUqzn9V_JnRtQadwWCbl"

    val rs429358Interval = Interval(44908683, 44908684)
    val rs429358locus = Location(rs429358Interval, chr19GRCh38SequenceIdentifier)
    val rs429358T = Allele(rs429358locus.getId(), "T")
    val rs429358C = Allele(rs429358locus.getId(), "C")

    val rs7412interval = Interval(44908821, 44908822)
    val rs7412locus = Location(rs7412interval, chr19GRCh38SequenceIdentifier)
    val rs7412T = Allele(rs7412locus.getId(), "T")
    val rs7412C = Allele(rs7412locus.getId(), "C")

    val ε1 = Haplotype(Completeness.COMPLETE, listOf(rs7412T.getId(), rs429358C.getId()))
    val ε2 = Haplotype(Completeness.COMPLETE, listOf(rs7412T.getId(), rs429358T.getId()))
    val ε3 = Haplotype(Completeness.COMPLETE, listOf(rs7412C.getId(), rs429358T.getId()))
    val ε4 = Haplotype(Completeness.COMPLETE, listOf(rs7412C.getId(), rs429358C.getId()))

    val ε2ε3 = Genotype(Completeness.COMPLETE, listOf(ε2.getId(), ε3.getId()))
    val ε3ε2 = Genotype(Completeness.COMPLETE, listOf(ε3.getId(), ε2.getId()))
    val ε4ε4 = Genotype(Completeness.COMPLETE, listOf(ε4.getId(), ε4.getId()))

    val meta = Meta(OffsetDateTime.parse("2017-06-07T06:13:59.38Z"), "0")

    @Test
    fun testIdentifier() {
        println(chr19GRCh38Identifier.getId())



//        "VMC:GL_9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo": [
//        {
//            "accession": "rs429358"
//        }
//        ],
//        "VMC:GL_LStELzYmlIQP3Zan9FhibgiFGAgSM7CI": [
//        {
//            "accession": "rs7412"
//        }
//        ],
//        "VMC:GS_IIB53T8CNeJJdUqzn9V_JnRtQadwWCbl": [
//        {
//            "accession": "NC_000019.10",
//            "namespace": "NCBI"
//        }
//        ]

    }

    @Test
    fun testInterval() {
        assertEquals("<Interval:44908683:44908684>", rs429358Interval.toVmc())
    }

    @Test
    fun testLocation() {
        val vmcString = rs429358locus.toVmc()
        assertEquals("<Location:<Identifier:VMC:GS_IIB53T8CNeJJdUqzn9V_JnRtQadwWCbl>:<Interval:44908683:44908684>>", vmcString)
        assertEquals("9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo", calculateDigest(vmcString))
        //to calculate the id the serialised vmcString is passed through the digest function to generate a 24 byte
        assertEquals("VMC:GL_9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo", rs429358locus.getId())

        println(prettyMapper.writeValueAsString(rs429358locus))
        val serialised: String = mapper.writeValueAsString(rs429358locus)
        val expected = "{\"id\":\"VMC:GL_9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo\",\"interval\":{\"start\":44908683,\"end\":44908684},\"sequence_id\":\"VMC:GS_IIB53T8CNeJJdUqzn9V_JnRtQadwWCbl\"}"
        assertEquals(expected, serialised)
    }

    @Test
    fun testAllele() {
        //TODO: these need to accept the Location, not the location identifier, same with all the other objects i.e. Location needs a Sequence which needs an id...
        assertEquals("<Allele:<Identifier:VMC:GL_9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo>:C>", rs429358C.toVmc())
        println(prettyMapper.writeValueAsString(rs429358C))
        val serialised: String = mapper.writeValueAsString(rs429358C)
        val expected = "{\"id\":\"VMC:GA_8vT5C3XyPLVz4_AXCI5P-J0gobxoGdxY\",\"location_id\":\"VMC:GL_9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo\",\"state\":\"C\"}"
        assertEquals(expected, serialised)
    }

    @Test
    fun testHaplotype() {
        println(ε1.toVmc())
        assertEquals("<Haplotype:COMPLETE:[<Identifier:VMC:GA_8vT5C3XyPLVz4_AXCI5P-J0gobxoGdxY>;<Identifier:VMC:GA_FABxPGRP7dT3sKot_91vXQrPvzfNYCbX>]>", ε1.toVmc())

        println(ε4.toVmc())
        assertEquals("<Haplotype:COMPLETE:[<Identifier:VMC:GA_8vT5C3XyPLVz4_AXCI5P-J0gobxoGdxY>;<Identifier:VMC:GA_Bdf7gntpo-snoQdY356RrpCuyrrzYynA>]>", ε4.toVmc())
        println(prettyMapper.writeValueAsString(ε4))
        val serialised: String = mapper.writeValueAsString(ε4)
        val expected = "{\"id\":\"VMC:GH_d3UvMyD-ArHLi-ZucGWxURhfeALz7arO\",\"completeness\":\"COMPLETE\",\"allele_ids\":[\"VMC:GA_8vT5C3XyPLVz4_AXCI5P-J0gobxoGdxY\",\"VMC:GA_Bdf7gntpo-snoQdY356RrpCuyrrzYynA\"]}"
        assertEquals(expected, serialised)

        val ε4AlleleOrderSwitched = Haplotype(Completeness.COMPLETE, listOf(rs429358C.getId(), rs7412C.getId()))
        assertEquals(expected,  mapper.writeValueAsString(ε4AlleleOrderSwitched), "Output should be the same regardless of allele order")
    }

    @Disabled
    @Test
    fun testGenotype() {
        assertEquals("<Genotype:COMPLETE:[<Identifier:VMC:GH_d3UvMyD-ArHLi-ZucGWxURhfeALz7arO>;<Identifier:VMC:GH_d3UvMyD-ArHLi-ZucGWxURhfeALz7arO>]>", ε4ε4.toVmc())

        assertEquals("<Genotype:COMPLETE:[<Identifier:VMC:GH_exlsvXjQFFhoMxc5IKUvdgOnMAbZ2oBh>;<Identifier:VMC:GH_SF_ZVWlwehopjxKDIF__paB1Q2DwjB4B>]>", ε2ε3.toVmc())

        println(calculateDigest(ε2ε3.toVmc()))
        val ε2ε3Json = "{\"id\":\"VMC:GG_ISiZFONyC1HHaBxi2kBklfDQEdb5CRRe\",\"completeness\":\"COMPLETE\",\"haplotype_ids\":[\"VMC:GH_exlsvXjQFFhoMxc5IKUvdgOnMAbZ2oBh\",\"VMC:GH_SF_ZVWlwehopjxKDIF__paB1Q2DwjB4B\"]}"
        assertEquals(ε2ε3Json, mapper.writeValueAsString(ε2ε3))

        assertEquals("<Genotype:COMPLETE:[<Identifier:VMC:GH_SF_ZVWlwehopjxKDIF__paB1Q2DwjB4B>;<Identifier:VMC:GH_exlsvXjQFFhoMxc5IKUvdgOnMAbZ2oBh>]>", ε3ε2.toVmc())
        val ε3ε2Json = "{\"id\":\"VMC:GG_ISiZFONyC1HHaBxi2kBklfDQEdb5CRRe\",\"completeness\":\"COMPLETE\",\"haplotype_ids\":[\"VMC:GH_SF_ZVWlwehopjxKDIF__paB1Q2DwjB4B\",\"VMC:GH_exlsvXjQFFhoMxc5IKUvdgOnMAbZ2oBh\"]}"
        assertEquals(ε3ε2Json, mapper.writeValueAsString(ε3ε2))
    }

    @Test
    fun testMeta() {
        println(mapper.writeValueAsString(meta))
    }

    @Test
    fun testBundle() {
        val vmcBundle = VmcBundle(meta,
                locations = listOf(rs429358locus, rs7412locus),
                alleles = listOf(rs429358T, rs429358C, rs7412T, rs7412C),
                haplotypes = listOf(ε1, ε2, ε3, ε4),
                genotypes = listOf(ε2ε3, ε3ε2, ε4ε4)
        )
        println(prettyMapper.writeValueAsString(vmcBundle))
    }

}
