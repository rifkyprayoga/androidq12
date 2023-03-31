package mega.privacy.android.app.presentation.rubbishbin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mega.privacy.android.app.domain.usecase.GetNodeByHandle
import mega.privacy.android.app.domain.usecase.GetRubbishBinChildren
import mega.privacy.android.app.domain.usecase.GetRubbishBinChildrenNode
import mega.privacy.android.app.presentation.data.NodeUIItem
import mega.privacy.android.domain.entity.SortOrder
import mega.privacy.android.domain.entity.node.Node
import mega.privacy.android.domain.entity.node.NodeChanges
import mega.privacy.android.domain.entity.node.NodeUpdate
import mega.privacy.android.domain.entity.preference.ViewType
import mega.privacy.android.domain.usecase.GetCloudSortOrder
import mega.privacy.android.domain.usecase.GetParentNodeHandle
import mega.privacy.android.domain.usecase.viewtype.MonitorViewType
import mega.privacy.android.domain.usecase.viewtype.SetViewType
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import test.mega.privacy.android.app.presentation.shares.FakeMonitorUpdates

@ExperimentalCoroutinesApi
class RubbishBinViewModelTest {

    private lateinit var underTest: RubbishBinViewModel

    private val getRubbishBinChildrenNode = mock<GetRubbishBinChildrenNode>()
    private val monitorNodeUpdates = FakeMonitorUpdates()
    private val getRubbishBinParentNodeHandle = mock<GetParentNodeHandle>()
    private val getRubbishBinChildren = mock<GetRubbishBinChildren>()
    private val getNodeByHandle = mock<GetNodeByHandle>()
    private val setViewType = mock<SetViewType>()
    private val monitorViewType = mock<MonitorViewType>()
    private val getCloudSortOrder = mock<GetCloudSortOrder>()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        initViewModel()
    }

    private fun initViewModel() {
        underTest = RubbishBinViewModel(
            getRubbishBinChildrenNode = getRubbishBinChildrenNode,
            monitorNodeUpdates = monitorNodeUpdates,
            getRubbishBinParentNodeHandle = getRubbishBinParentNodeHandle,
            getRubbishBinChildren = getRubbishBinChildren,
            getNodeByHandle = getNodeByHandle,
            setViewType = setViewType,
            monitorViewType = monitorViewType,
            getCloudSortOrder = getCloudSortOrder
        )
    }

    @Test
    fun `test that initial state is returned`() = runTest {
        underTest.state.test {
            val initial = awaitItem()
            Truth.assertThat(initial.rubbishBinHandle).isEqualTo(-1L)
            Truth.assertThat(initial.nodes).isEmpty()
        }
    }

    @Test
    fun `test that rubbish bin handle is updated if new value provided`() = runTest {
        underTest.state.map { it.rubbishBinHandle }.distinctUntilChanged()
            .test {
                val newValue = 123456789L
                Truth.assertThat(awaitItem()).isEqualTo(-1L)
                underTest.setRubbishBinHandle(newValue)
                Truth.assertThat(awaitItem()).isEqualTo(newValue)
            }
    }

    @Test
    fun `test that on setting rubbish bin handle rubbish bin node returns empty list`() =
        runTest {
            val newValue = 123456789L
            whenever(getRubbishBinChildrenNode.invoke(newValue)).thenReturn(ArrayList())
            monitorNodeUpdates.emit(NodeUpdate(emptyMap()))
            underTest.setRubbishBinHandle(newValue)
            Truth.assertThat(underTest.state.value.nodes.size).isEqualTo(0)
        }

    @Test
    fun `test that on setting rubbish bin handle rubbish bin node returns some items in list`() =
        runTest {
            val newValue = 123456789L
            whenever(getRubbishBinChildrenNode.invoke(newValue)).thenReturn(
                listOf(
                    mock(),
                    mock()
                )
            )
            whenever(getRubbishBinChildren(newValue)).thenReturn(
                listOf(mock(), mock())
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)

            val update = mapOf<Node, List<NodeChanges>>(
                mock<Node>() to emptyList(),
                mock<Node>() to emptyList()
            )
            monitorNodeUpdates.emit(NodeUpdate(update))
            underTest.setRubbishBinHandle(newValue)
            Truth.assertThat(underTest.state.value.nodes.size).isEqualTo(2)
        }

    @Test
    fun `test that on setting rubbish bin handle rubbish bin node returns null`() = runTest {
        val newValue = 123456789L
        whenever(getRubbishBinChildrenNode.invoke(newValue)).thenReturn(null)
        whenever(getRubbishBinChildren(newValue)).thenReturn(emptyList())
        underTest.setRubbishBinHandle(newValue)
        Truth.assertThat(underTest.state.value.nodes.size).isEqualTo(0)
        verify(getRubbishBinChildrenNode, times(1)).invoke(newValue)
    }

    @Test
    fun `test that when folder is clicked from adapter, then stack gets updated with appropriate value`() =
        runTest {
            val lastFirstVisiblePosition = 123456
            val newValue = 12345L

            whenever(getRubbishBinChildrenNode.invoke(newValue)).thenReturn(
                listOf(
                    mock(),
                    mock()
                )
            )
            val update = mapOf<Node, List<NodeChanges>>(
                mock<Node>() to emptyList(),
                mock<Node>() to emptyList()
            )
            monitorNodeUpdates.emit(NodeUpdate(update))
            underTest.setRubbishBinHandle(newValue)

            underTest.onFolderItemClicked(lastFirstVisiblePosition, newValue)
            Truth.assertThat(underTest.popLastPositionStack()).isEqualTo(lastFirstVisiblePosition)
        }

    @Test
    fun `test that last position returns 0 when items are popped from stack and stack has no items`() {
        val poppedValue = underTest.popLastPositionStack()
        Truth.assertThat(poppedValue).isEqualTo(0)
    }

    @Test
    fun `test that when handle on back pressed and parent handle is null, then getRubbishBinChildrenNode is not invoked`() =
        runTest {
            val newValue = 123456789L
            underTest.onBackPressed()
            verify(getRubbishBinChildrenNode, times(0)).invoke(newValue)
        }

    @Test
    fun `test that when handle on back pressed and parent handle is not null, then getRubbishBinChildrenNode is invoked once`() =
        runTest {
            val newValue = 123456789L
            // to update handles rubbishBinHandle
            whenever(getRubbishBinChildren(newValue)).thenReturn(emptyList())
            underTest.setRubbishBinHandle(newValue)
            underTest.onBackPressed()
            verify(getRubbishBinChildrenNode, times(1)).invoke(newValue)
        }

    @Test
    fun `test when when nodeUIItem is long clicked, then it updates selected item by 1`() =
        runTest {
            val nodesListItem1 = mock<Node>()
            val nodesListItem2 = mock<Node>()
            whenever(nodesListItem1.id.longValue).thenReturn(1L)
            whenever(nodesListItem2.id.longValue).thenReturn(2L)
            whenever(getRubbishBinChildrenNode(underTest.state.value.rubbishBinHandle)).thenReturn(
                listOf(mock(), mock())
            )
            whenever(getRubbishBinChildren(underTest.state.value.rubbishBinHandle)).thenReturn(
                listOf(nodesListItem1, nodesListItem2)
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)
            underTest.refreshNodes()
            underTest.onLongItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = true,
                    isInvisible = false
                )
            )
            underTest.state.test {
                val state = awaitItem()
                Truth.assertThat(state.selectedNodes).isEqualTo(1)
            }
        }

    @Test
    fun `test that when item is clicked and some items are already selected on list then checked index gets decremented by 1`() =
        runTest {
            val nodesListItem1 = mock<Node>()
            val nodesListItem2 = mock<Node>()
            whenever(nodesListItem1.id.longValue).thenReturn(1L)
            whenever(nodesListItem2.id.longValue).thenReturn(2L)
            whenever(getRubbishBinChildrenNode(underTest.state.value.rubbishBinHandle)).thenReturn(
                listOf(mock(), mock())
            )
            whenever(getRubbishBinChildren(underTest.state.value.rubbishBinHandle)).thenReturn(
                listOf(nodesListItem1, nodesListItem2)
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)

            underTest.refreshNodes()
            underTest.onLongItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = true,
                    isInvisible = false
                )
            )
            underTest.onItemClicked(
                NodeUIItem(
                    nodesListItem2,
                    isSelected = true,
                    isInvisible = false
                )
            )
            underTest.state.test {
                val state = awaitItem()
                Truth.assertThat(state.selectedNodes).isEqualTo(0)
            }
        }

    @Test
    fun `test that when selected item gets clicked then checked index gets incremented by 1`() =
        runTest {
            val nodesListItem1 = mock<Node>()
            val nodesListItem2 = mock<Node>()
            whenever(nodesListItem1.id.longValue).thenReturn(1L)
            whenever(nodesListItem2.id.longValue).thenReturn(2L)
            whenever(getRubbishBinChildrenNode(underTest.state.value.rubbishBinHandle)).thenReturn(
                listOf(mock(), mock())
            )
            whenever(getRubbishBinChildren(underTest.state.value.rubbishBinHandle)).thenReturn(
                listOf(nodesListItem1, nodesListItem2)
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)

            underTest.refreshNodes()
            underTest.onLongItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = true,
                    isInvisible = false
                )
            )
            underTest.onItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = false,
                    isInvisible = false
                )
            )
            underTest.state.test {
                val state = awaitItem()
                Truth.assertThat(state.selectedNodes).isEqualTo(2)
            }
        }

    @Test
    fun `test that on clicking on change view type to Grid it calls setViewType atleast once`() =
        runTest {
            underTest.onChangeViewTypeClicked()
            verify(setViewType, times(1)).invoke(ViewType.GRID)
        }
}
