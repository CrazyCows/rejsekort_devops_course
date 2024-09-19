package devops.rejsekort.model

class UserData {
    val firstName: String
    val lastName: String
    val isCheckedIn: Boolean


    constructor(firstName: String, lastName: String, isCheckedIn: Boolean) {
        this.firstName = firstName
        this.lastName = lastName
        this.isCheckedIn = isCheckedIn
    }


}