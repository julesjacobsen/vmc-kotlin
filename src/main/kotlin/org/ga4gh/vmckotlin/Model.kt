package org.ga4gh.vmckotlin

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.OffsetDateTime

import java.util.*

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */


object Vmc {
    val namespace = "VMC"
}

interface SerializableVariantModel {

    fun toVmc() : String

    fun getId(): String
}

/**
 * A <namespace,accession> pair that refers to an object.
 */
data class Identifier(val namespace: String, val accession: String) : SerializableVariantModel {

    override fun toVmc(): String {
        return "<Identifier:$namespace:$accession>"
    }

    @JsonIgnore
    override fun getId(): String {
        return ""
    }
}

//TODO: How's this supposed to work? Its not used in the Python example.
data class Sequence(val sequence: String)

/**
 * A <start,end> position pair, using interbase coordinates.
 */
data class Interval(val start: Int, val end: Int) : SerializableVariantModel {

    override fun toVmc(): String {
        return "<Interval:$start:$end>"
    }

    @JsonIgnore
    override fun getId(): String {
        return ""
    }
}

/**
 * An Interval on a Sequence.
 */
@JsonPropertyOrder("id", "interval", "sequence_id")
data class Location(val interval: Interval, @JsonProperty("sequence_id") val sequenceId: String) : SerializableVariantModel {

    override fun toVmc(): String {
        //<Location:<Identifier:VMC:GS_IIB53T8CNeJJdUqzn9V_JnRtQadwWCbl>:<Interval:44908683:44908684>>
        return "<Location:<Identifier:$sequenceId>:${interval.toVmc()}>"
    }

    override fun getId(): String {
        return "${Vmc.namespace}:GL_${calculateDigest(toVmc())}"
    }
}

/**
 * A contiguous change at a Location.
 */
@JsonPropertyOrder("id", "location_id", "state")
data class Allele(@JsonProperty("location_id") val locationId: String, val state: String) : SerializableVariantModel {

    override fun toVmc(): String {
        //<Allele:<Identifier:VMC:GL_9Jht-lguk_jnBvG-wLJbjmBw5v_v7rQo>:C>
        return "<Allele:<Identifier:$locationId>:$state>"
    }

    //TODO: return an actual Identifier
    override fun getId(): String {
        return "${Vmc.namespace}:GA_${calculateDigest(toVmc())}"
    }
}

/**
 * Declares the `completeness` of haplotype or genotype definitions.
 */
enum class Completeness {UNKNOWN, PARTIAL, COMPLETE }

/**
 * A set of zero or more Alleles on a single instance of a Sequence.
 */
@JsonPropertyOrder("id", "completeness", "allele_ids")
data class Haplotype(val completeness: Completeness, @JsonIgnore val alleles: List<String>) : SerializableVariantModel {

    @JsonProperty("allele_ids")
    val alleleIds = alleles.sorted()

    override fun toVmc(): String {
        //<Haplotype:COMPLETE:[<Identifier:VMC:GA_8vT5C3XyPLVz4_AXCI5P-J0gobxoGdxY>;<Identifier:VMC:GA_FABxPGRP7dT3sKot_91vXQrPvzfNYCbX>]>
        return "<Haplotype:$completeness:${toVmcList(alleleIds)}>"
    }

    override fun getId(): String {
        return "${Vmc.namespace}:GH_${calculateDigest(toVmc())}"
    }
}

/**
 * A list of Haplotypes.
 */
@JsonPropertyOrder("id", "completeness", "haplotype_ids")
data class Genotype(val completeness: Completeness, @JsonProperty("haplotype_ids") val haplotypeIds: List<String>): SerializableVariantModel {

    override fun toVmc(): String {
        //<Genotype:COMPLETE:[<Identifier:VMC:GH_d3UvMyD-ArHLi-ZucGWxURhfeALz7arO>;<Identifier:VMC:GH_d3UvMyD-ArHLi-ZucGWxURhfeALz7arO>]>
        return "<Genotype:$completeness:${toVmcList(haplotypeIds)}>"
    }

    override fun getId(): String {
        return "${Vmc.namespace}:GG_${calculateDigest(toVmc())}"
    }
}

data class Meta(val generatedAt: OffsetDateTime, val version: String = "0")

data class VmcBundle(@JsonProperty val meta: Meta = Meta(OffsetDateTime.now()),
                     @JsonIgnore val locations: List<Location> = emptyList(),
                     @JsonIgnore val alleles: List<Allele> = emptyList(),
                     @JsonIgnore val haplotypes: List<Haplotype> = emptyList(),
                     @JsonIgnore val genotypes: List<Genotype> = emptyList()) {

    @JsonProperty("locations")
    val locationMap = locations.associateBy({it.getId()}, {it})

    @JsonProperty("alleles")
    val alleleMap = alleles.associateBy({it.getId()}, {it})

    @JsonProperty("haplotypes")
    val haplotypeMap = haplotypes.associateBy({it.getId()}, {it})

    @JsonProperty("genotypes")
    val genotypeMap = genotypes.associateBy({it.getId()}, {it})

}

private fun toVmcList(ids: List<String>): String {
    val stringBuilder = StringBuilder("[")
    val stringJoiner = StringJoiner(";")
    ids.forEach{String -> stringJoiner.add("<Identifier:$String>")}
    stringBuilder.append(stringJoiner.toString())
    stringBuilder.append("]")
    return stringBuilder.toString()
}

fun calculateDigest(vmcSerialized: String): String {
    val bytes = vmcSerialized.toByteArray(Charsets.US_ASCII)
    val sha512t241 = sha512t24(bytes)
    return Base64.getUrlEncoder().encodeToString(sha512t241)
}

/**
 * return the 24-byte truncated SHA-512 for input `bytes`
 */
private fun sha512t24(bytes: ByteArray): ByteArray {
    var md: MessageDigest? = null
    try {
        md = MessageDigest.getInstance("SHA-512")
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    val digest = md!!.digest(bytes)
    val digest24 = Arrays.copyOfRange(digest, 0, 24)
    return digest24
}
