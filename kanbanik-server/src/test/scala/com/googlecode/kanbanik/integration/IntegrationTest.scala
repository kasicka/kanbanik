package com.googlecode.kanbanik.integration

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import com.googlecode.kanbanik.builders.WorkflowitemTestManipulation
import com.googlecode.kanbanik.commands.AddProjectsToBoardCommand
import com.googlecode.kanbanik.commands.EditWorkflowCommand
import com.googlecode.kanbanik.commands.EditWorkflowitemDataCommand
import com.googlecode.kanbanik.commands.GetAllBoardsCommand
import com.googlecode.kanbanik.commands.MoveTaskCommand
import com.googlecode.kanbanik.commands.SaveBoardCommand
import com.googlecode.kanbanik.commands.SaveProjectCommand
import com.googlecode.kanbanik.commands.SaveTaskCommand
import com.googlecode.kanbanik.commands.DeleteTaskCommand
import com.googlecode.kanbanik.commands.DeleteWorkflowitemCommand
import com.googlecode.kanbanik.commands.DeleteProjectCommand
import com.googlecode.kanbanik.commands.DeleteBoardCommand
import com.googlecode.kanbanik.dto.BoardDto
import com.googlecode.kanbanik.dto.BoardWithProjectsDto
import com.googlecode.kanbanik.dto.ProjectDto
import com.googlecode.kanbanik.dto.TaskDto
import com.googlecode.kanbanik.dto.WorkflowDto
import com.googlecode.kanbanik.dto.WorkflowitemDto
import com.googlecode.kanbanik.dto.ClassOfService
import com.googlecode.kanbanik.dto.shell.EditWorkflowParams
import com.googlecode.kanbanik.dto.shell.MoveTaskParams
import com.googlecode.kanbanik.dto.shell.SimpleParams
import com.googlecode.kanbanik.dto.shell.VoidParams
import com.googlecode.kanbanik.model.DbCleaner
import com.googlecode.kanbanik.model.Project
import com.googlecode.kanbanik.dto.GetAllBoardsWithProjectsParams

/**
 * This are tests which expects working DB and are trying to simmulate some basic use
 * cases of the users. They are really calling commands and so on.
 */
@RunWith(classOf[JUnitRunner])
class IntegrationTests extends FlatSpec with BeforeAndAfter with WorkflowitemTestManipulation {
  "Kanbanik" should "be able to create a new setup from scratch" in {
      val start = System.currentTimeMillis()
    // creation phase
    
    // create board
    val board = new BoardDto()
    board.setName("board1")
    board.setWorkflow(new WorkflowDto())
    val storedBoard = new SaveBoardCommand().execute(new SimpleParams(board))
    
    // create project
    val project = new ProjectDto()
    project.setName("project1")
    val storedProject = new SaveProjectCommand().execute(new SimpleParams(project))
    
    // assign project to board
    val boardWithProjects = new BoardWithProjectsDto()
    boardWithProjects.setBoard(storedBoard.getPayload().getPayload())
    boardWithProjects.addProject(storedProject.getPayload().getPayload())
    val storedBoardWithProjects = new AddProjectsToBoardCommand().execute(new SimpleParams(boardWithProjects)).getPayload().getPayload()

    // create workflow
    val workflow = storedBoardWithProjects.getBoard().getWorkflow()
    val item1 = itemDtoWithName("item1", storedBoardWithProjects.getBoard().getWorkflow())
    val item2 = itemDtoWithName("item2", storedBoardWithProjects.getBoard().getWorkflow())
    val item3 = itemDtoWithName("item3", storedBoardWithProjects.getBoard().getWorkflow())
    
    editWorkflow(itemDtoWithName("item1", loadWorkflow), null, loadWorkflow)
    editWorkflow(itemDtoWithName("item2", loadWorkflow), null, loadWorkflow)
    editWorkflow(itemDtoWithName("item3", loadWorkflow), null, loadWorkflow)
    
    assert(loadAllBoards.map(dto => dto.getBoard.getName()) === List("board1"))
    assert(loadProject().getName() === "project1")

    assert(asWorkflowList(loadWorkflow).map(_.getName()) === List("item1", "item2", "item3"))
    
    val taskDto = new TaskDto()
    taskDto.setName("taskName1")
    taskDto.setDescription("desc")
    taskDto.setClassOfService(ClassOfService.EXPEDITE)
    taskDto.setProject(storedProject.getPayload().getPayload())
    taskDto.setWorkflowitem(loadWorkflow.getWorkflowitems().get(0))
    
    val storedTask = new SaveTaskCommand().execute(new SimpleParams(taskDto))
    assert(storedTask.getPayload().getPayload().getName() === "taskName1")
    
    // edit phase

    // edit workflow
    editWorkflow(loadItem(2), loadItem(0), loadWorkflow)
    assert(asWorkflowList(loadWorkflow).map(_.getName()) === List("item3", "item1", "item2"))

    // edit workflowitem
    val itemToEdit = loadItem(1)
    itemToEdit.setName("item1_renamed")
    new EditWorkflowitemDataCommand().execute(new SimpleParams(itemToEdit))
    assert(asWorkflowList(loadWorkflow).map(_.getName()) === List("item3", "item1_renamed", "item2"))
    
    // edit task
    val taskToMove = loadBoard().getTasks().get(0)
    taskToMove.setWorkflowitem(loadWorkflow.getWorkflowitems().get(2))
    val moveTaskParams = new MoveTaskParams(taskToMove, loadProject(), null, null)
    val movedTask = new MoveTaskCommand().execute(moveTaskParams)
    assert(movedTask.getPayload().getPayload().getWorkflowitem().getName() === "item2")
    assert(loadBoard().getTasks().get(0).getWorkflowitem().getName() === "item2")
    
    // edit board
    val boardToEdit = loadBoard()
    boardToEdit.setName("board1_renamed")
    val editedBoard = new SaveBoardCommand().execute(new SimpleParams(boardToEdit))
    assert(editedBoard.getPayload().getPayload().getName() === "board1_renamed")
    // verify it did not destroy the workflow
    assert(asWorkflowList(loadWorkflow).map(_.getName()) === List("item3", "item1_renamed", "item2"))
    
    // edit project
    val projectToEdit = loadProject()
    projectToEdit.setName("project1_renamed")
    val editProject = new SaveProjectCommand().execute(new SimpleParams(projectToEdit))
    assert(editProject.getPayload().getPayload().getName() === "project1_renamed")
    assert(Project.all.size === 1)
    // delete phase
    
    // delete task
    new DeleteTaskCommand().execute(new SimpleParams(movedTask.getPayload().getPayload()))
    assert(loadBoard().getTasks().size() === 0)
    
    // delete workflowitems
    val loadedItem1 = loadItem(0)
    val loadedItem2 = loadItem(1)
    val loadedItem3 = loadItem(2)
    
    new DeleteWorkflowitemCommand().execute(new SimpleParams(loadedItem1))
    new DeleteWorkflowitemCommand().execute(new SimpleParams(loadedItem2))
    new DeleteWorkflowitemCommand().execute(new SimpleParams(loadedItem3))
    
    assert(loadWorkflowitems().size() === 0)
    
    // delete project
    new DeleteProjectCommand().execute(new SimpleParams(loadProject))
    assert(loadAllBoards.head.getProjectsOnBoard().size() === 0)
    
    // delete board
    new DeleteBoardCommand().execute(new SimpleParams(loadBoard))
    assert(loadAllBoards.size === 0)
  }
  
  def loadProject() = loadAllBoards.head.getProjectsOnBoard().get(0)
  
  def loadBoard() = loadAllBoards().head.getBoard()
  
  def loadWorkflow() = loadAllBoards.head.getBoard().getWorkflow()

  def loadWorkflowitems() = loadBoard().getWorkflow().getWorkflowitems()
  
  def loadItem(item: Int): WorkflowitemDto = {
    loadWorkflowitems().get(item)
  }
  
  def loadAllBoards() = {
	  new GetAllBoardsCommand().execute(new GetAllBoardsWithProjectsParams(true)).getPayload().getList().toArray().toList.asInstanceOf[List[BoardWithProjectsDto]]    
  }
  
  def asWorkflowList(workflow: WorkflowDto) = {
    workflow.getWorkflowitems().toArray().toList.asInstanceOf[List[WorkflowitemDto]]
  }
  
  def editWorkflow(item: WorkflowitemDto, nextItem: WorkflowitemDto, workflow: WorkflowDto) {
    val editWorkflowParams = new EditWorkflowParams(
    		item,
    		nextItem,
    		workflow
    )
    
    new EditWorkflowCommand().execute(editWorkflowParams)
  }

  after {
    // cleanup database
    DbCleaner.clearDb
  }
}