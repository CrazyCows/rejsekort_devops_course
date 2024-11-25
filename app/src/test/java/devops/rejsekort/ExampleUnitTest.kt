package devops.rejsekort

import devops.rejsekort.viewModels.RejsekortViewmodel
import org.junit.Test

import org.junit.Assert.*
import kotlin.random.Random

class RejsekortViewmodelTest{
    val rejsekortViewmodel: RejsekortViewmodel = RejsekortViewmodel()

    @Test
    fun checkSetUserData(){
        //essentially testing the getter and setter. A bit overkill, but there is not much
        //to test in a front end with no business logic.
        for(i in 1..10000){
            val randomfname = generateRandomString()
            val randomlname = generateRandomString()
            val randomusertoken = generateRandomString()

            rejsekortViewmodel.setUserData(randomfname, randomlname, randomusertoken)

            assertEquals(rejsekortViewmodel.userData.value.firstName, randomfname)
            assertEquals(rejsekortViewmodel.userData.value.lastName, randomlname)
            assertEquals(rejsekortViewmodel.userData.value.token, randomusertoken)
        }
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}


fun generateRandomString(): String {
    val length = Random.nextInt(1, 33) // Random length between 1 and 32
    val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') // Allowed characters
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
