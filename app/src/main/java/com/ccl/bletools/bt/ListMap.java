package com.ccl.bletools.bt;import java.util.ArrayList;

public class ListMap<K, V> {
	private Object iSyncLock = new Object();
	
	private ArrayList<K> iKeys = null;
	private ArrayList<V> iValues = null;
	
	public ListMap(){
		
	}
	
	public ListMap(ListMap<? extends K, ? extends V>aList){
		if (aList != null){
			iKeys = new ArrayList<K>(aList.iKeys);
			iValues = new ArrayList<V>(aList.iValues);
		}
	}
	
	public boolean add(K aKey, V aValue){
		return add(aKey, aValue, -1);
	}
	
	public boolean add(K aKey, V aValue, int aIndex){
		if (aKey != null){
			if (iKeys == null){
				iKeys = new ArrayList<K>();
			}
			
			if (iValues == null){
				iValues = new ArrayList<V>();
			}
			
			if (iKeys.contains(aKey)){
				return false;
			}else{
				if (aIndex > -1 && aIndex < iValues.size()){
					iValues.add(aIndex, aValue);
					iKeys.add(aIndex, aKey);
					
					return true;		
				}else{
					if (iValues.add(aValue)){
						if (iKeys.add(aKey)){
							return true;
						}else{
							iValues.remove(aKey);
						}
					}					
				}
			}
		}
		
		return false;
	}
	
	public boolean put(K aKey, V aValue){
		if (iKeys != null && iValues != null){
			int tIndex = iKeys.indexOf(aKey);
			
			if (tIndex != -1){
				if (removeAt(tIndex)){
					return add(aKey, aValue, tIndex);
				}
			}else{
				return add(aKey, aValue);
			}
		}else{
			return add(aKey, aValue);
		}
		
		return false;
	}
	
	public int size(){
		if (iKeys != null){
			return iKeys.size();
		}
		
		return 0;
	}
	
	public boolean isEmpty(){
		if (iKeys != null){
			return iKeys.isEmpty();
		}
		
		return true;
	}
	
	public boolean containsKey(K aKey){
		if (iKeys != null){
			return iKeys.contains(aKey);
		}
		
		return false;
	}
	
	public boolean containsValue(V aValue){
		if (iValues != null){
			return iValues.contains(aValue);
		}
		
		return false;
	}
	
	public int indexOfKey(K aKey){
		if (aKey != null && iKeys != null){
			return iKeys.indexOf(aKey);
		}
		
		return -1;
	}
	
	public int indexOfValue(V aValue){
		if (aValue != null && iValues != null){
			return iValues.indexOf(aValue);
		}
		
		return -1;
	}
	
	public K getKey(V aValue){
		return getKeyAt(indexOfValue(aValue));
	}
	
	public K getKeyAt(int aIndex){
		synchronized(iSyncLock){
			if (iKeys != null){
				if (aIndex > -1 && aIndex < iKeys.size()){
					return iKeys.get(aIndex);
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<K> getKeys(){
		return iKeys;
	}
	
	public V getValue(K aKey){
		return getValueAt(indexOfKey(aKey));
	}
	
	public V getValueAt(int aIndex){
		synchronized(iSyncLock){
			if (iValues != null){
				if (aIndex > -1 && aIndex < iValues.size()){
					return iValues.get(aIndex);
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<V> getValues(){
		return iValues;
	}
	
	public boolean removeAt(int aIndex){
		synchronized(iSyncLock){
			if (iKeys != null && iValues != null){
				if (aIndex > -1 && aIndex < iValues.size()){
					if (iValues.remove(aIndex) != null){
						return iKeys.remove(aIndex) != null;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean remove(K aKey){
		if (aKey != null && iKeys != null && iValues != null){
			return removeAt(iKeys.indexOf(aKey));
		}
		
		return false;
	}
	
	public int removeAll(V aValue){
		int tCount = 0;
		
		if (aValue != null && iValues != null){
			int tIndex;
			
			while ((tIndex = iValues.indexOf(aValue)) != -1){
				if (removeAt(tIndex)){
					tCount++;
				}else{
					break;
				}
			}
		}
		
		return tCount;
	}
	
	public void clear(){
		synchronized(iSyncLock){
			if (iKeys != null && iValues != null){
				iKeys.clear();
			
				iValues.clear();
			}
		}
	}
	
	public String[] toStringArray(String aDelimiter){
		if (aDelimiter == null){
			aDelimiter = ",";
		}
		
		synchronized(iSyncLock){
			if (iKeys != null){
				int tCount = iKeys.size();
				String tArray[] = new String[tCount];
				
				if (tCount > 0){
					for (int i = 0; i < tCount; i++){
						tArray[i] = iKeys.get(i).toString() + aDelimiter + iValues.get(i).toString();
					}
				}
				
				return tArray;
			}
		}
		
		return null;
	}

	@Override
	public String toString() {
		synchronized(iSyncLock){
			if (iKeys != null){
				int tSize = iKeys.size();
				
				if (tSize > 0){
					StringBuilder tString = new StringBuilder();
					
					for (int i = 0; i < tSize; i++){
						if (i > 0){
							tString.append(",");
						}
						
						tString.append(iKeys.get(i).toString() + "=" + iValues.get(i).toString());
					}
					
					return tString.toString();
				}
			}
			
			return "EMPTY";
		}
	}
}
