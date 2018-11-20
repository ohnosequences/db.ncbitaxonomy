package ohnosequences.db.ncbitaxonomy

sealed abstract class Rank

/** Holds all the possible `Rank`s for a taxonomic node */
case object Rank {
  case object Superkingdom    extends Rank
  case object Kingdom         extends Rank
  case object Subkingdom      extends Rank
  case object Superphylum     extends Rank
  case object Phylum          extends Rank
  case object Subphylum       extends Rank
  case object Superclass      extends Rank
  case object Class           extends Rank
  case object Subclass        extends Rank
  case object Infraclass      extends Rank
  case object Cohort          extends Rank
  case object Superorder      extends Rank
  case object Order           extends Rank
  case object Suborder        extends Rank
  case object Infraorder      extends Rank
  case object Parvorder       extends Rank
  case object Superfamily     extends Rank
  case object Family          extends Rank
  case object Subfamily       extends Rank
  case object Tribe           extends Rank
  case object Subtribe        extends Rank
  case object Genus           extends Rank
  case object Subgenus        extends Rank
  case object SpeciesGroup    extends Rank
  case object SpeciesSubgroup extends Rank
  case object Species         extends Rank
  case object Subspecies      extends Rank
  case object Varietas        extends Rank
  case object Forma           extends Rank
  case object NoRank          extends Rank

  /** All the possible ranks */
  val all: Set[Rank] = Set(
    Superkingdom,
    Kingdom,
    Subkingdom,
    Superphylum,
    Phylum,
    Subphylum,
    Superclass,
    Class,
    Subclass,
    Infraclass,
    Cohort,
    Superorder,
    Order,
    Suborder,
    Infraorder,
    Parvorder,
    Superfamily,
    Family,
    Subfamily,
    Tribe,
    Subtribe,
    Genus,
    Subgenus,
    SpeciesGroup,
    SpeciesSubgroup,
    Species,
    Subspecies,
    Varietas,
    Forma,
    NoRank
  )

  /** Returns, if it is possible to parse it, a `Rank` from a `String`
    *
    * @param str the `String` we want to parse
    * @return `Some(rank)` if it was possible to parse the `Rank`, None
    * otherwise
    */
  def apply(str: String): Option[Rank] =
    // Normalize str: remove all whitespaces, make it lower case
    str.replaceAll("\\s", "").toLowerCase match {
      case "superkingdom"    => Some(Superkingdom)
      case "kingdom"         => Some(Kingdom)
      case "subkingdom"      => Some(Subkingdom)
      case "superphylum"     => Some(Superphylum)
      case "phylum"          => Some(Phylum)
      case "subphylum"       => Some(Subphylum)
      case "superclass"      => Some(Superclass)
      case "class"           => Some(Class)
      case "subclass"        => Some(Subclass)
      case "infraclass"      => Some(Infraclass)
      case "cohort"          => Some(Cohort)
      case "superorder"      => Some(Superorder)
      case "order"           => Some(Order)
      case "suborder"        => Some(Suborder)
      case "infraorder"      => Some(Infraorder)
      case "parvorder"       => Some(Parvorder)
      case "superfamily"     => Some(Superfamily)
      case "family"          => Some(Family)
      case "subfamily"       => Some(Subfamily)
      case "tribe"           => Some(Tribe)
      case "subtribe"        => Some(Subtribe)
      case "genus"           => Some(Genus)
      case "subgenus"        => Some(Subgenus)
      case "speciesgroup"    => Some(SpeciesGroup)
      case "speciessubgroup" => Some(SpeciesSubgroup)
      case "species"         => Some(Species)
      case "subspecies"      => Some(Subspecies)
      case "varietas"        => Some(Varietas)
      case "forma"           => Some(Forma)
      case "norank"          => Some(NoRank)
      case _                 => None
    }
}
