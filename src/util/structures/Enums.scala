package util.structures

/**
 * Set of enums
 * Created by CAB on 22.09.14.
 */

trait FoundBy
object FoundBy{
  case object Unknown extends FoundBy
  case object Search extends FoundBy
  case object Analyse extends FoundBy
  def formString(s:String):FoundBy = s match{
    case "Unknown" => Unknown
    case "Search" => Search
    case "Analyse" => Analyse
    case _ => throw new Exception("Error on parse: " + s)}}

trait Payment
object Payment{
  case object Unknown extends Payment
  case object Hourly extends Payment
  case object Budget extends Payment}

trait PaymentMethod
object PaymentMethod{
  case object Unknown extends PaymentMethod
  case object Verified extends PaymentMethod
  case object No extends PaymentMethod}

trait Employment
object Employment{
  case object Unknown extends Employment
  case object AsNeeded extends Employment
  case object Full extends Employment
  case object Part extends Employment}

trait InitiatedBy
object InitiatedBy{
  case object Unknown extends InitiatedBy
  case object Freelancer extends InitiatedBy
  case object Client extends InitiatedBy}

trait SkillLevel
object SkillLevel{
  case object Unknown extends SkillLevel
  case object Entry extends SkillLevel
  case object Intermediate extends SkillLevel
  case object Expert extends SkillLevel}

trait WorkerType
object WorkerType{
  case object Unknown extends WorkerType
  case object Freelancer extends WorkerType
  case object Manager extends WorkerType}

trait JobState
object JobState{
  case object Unknown extends JobState
  case object InProcess extends JobState
  case object End extends JobState}

trait JobAvailable
object JobAvailable{
  case object Unknown extends JobAvailable
  case object Yes extends JobAvailable
  case object No extends JobAvailable
  def formString(s:String):JobAvailable = s match{
    case "Unknown" => Unknown
    case "Yes" => Yes
    case "No" => No
    case _ => throw new Exception("Error on parse: " + s)}}

trait FreelancerAvailable
object FreelancerAvailable{
  case object Unknown extends FreelancerAvailable
  case object FullTime extends FreelancerAvailable
  case object PartTime extends FreelancerAvailable
  case object LessThen extends FreelancerAvailable
  def formString(s:String):FreelancerAvailable = s match{
    case "Unknown" => Unknown
    case "FullTime" => FullTime
    case "PartTime" => PartTime
    case "LessThen" => LessThen
    case _ => throw new Exception("Error on parse: " + s)}}

trait Update
object Update{
  case object Unknown extends Update
  case object NeedUpdate extends Update
  case object Updated extends Update
  def formString(s:String):Update = s match{
    case "Unknown" => Unknown
    case "NeedUpdate" => NeedUpdate
    case "Updated" => Updated
    case _ => throw new Exception("Error on parse: " + s)}}

