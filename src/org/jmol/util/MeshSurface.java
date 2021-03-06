package org.jmol.util;

import java.util.Hashtable;
import java.util.Map;

import org.jmol.java.BS;
import org.jmol.script.T;

import javajs.util.AU;
import javajs.util.Lst;
import javajs.util.Measure;
import javajs.util.PT;
import javajs.util.SB;
import javajs.util.M4;
import javajs.util.P3;
import javajs.util.P4;
import javajs.util.V3;
import javajs.util.T3;

public class MeshSurface {
  
  protected static final int SEED_COUNT = 25;

  public V3[] spanningVectors;

  public String meshType;

  /**
   * vertex count
   */
  public int vc;
  /**
   * vertices
   */
  public T3[] vs;
  /**
   * vertex values
   */
  public float[] vvs;
  public int[] vertexSource;

  /**
   * polygon count
   */
  public int pc;
  /**
   * polygon indexes
   */
  public int[][] pis;
  public float[] polygonTranslucencies;

  public boolean isTriangleSet; // just a set of flat polygons
  public boolean haveQuads;
  public short colix;
  public short colixBack;

  public boolean isColorSolid = true;
  public P3 offset;
  public T3[] altVertices;

  /**
   * polygon color index "colix" array
   */
  public short[] pcs;
  /**
   * vertex color index "colix" array
   */
  public short[] vcs;
  public T3[] normals; // for export only or for cartoons
  public V3[] normalsTemp; // for cartoons
  public int normalCount; // for export only
  public int normixCount;
  public BS bsPolygons;
  public M4 mat4;
  public BS[] surfaceSet;
  public int[] vertexSets;
  public int nSets = 0;

  private int checkCount = 2;

  /**
   * 
   * @j2sIgnore
   */
  public MeshSurface() {
  }

  public static MeshSurface newMesh(boolean isAlt, T3[] vertices,
                                    int vertexCount, int[][] polygonIndexes,
                                    T3[] normals, int nNormals) {
    MeshSurface ms = new MeshSurface();
    ms.pis = polygonIndexes;
    if (isAlt)
      ms.altVertices = vertices;
    else
      ms.vs = vertices;
    ms.vc = (vertexCount == 0 ? vertices.length : vertexCount);
    ms.normals = normals;
    ms.normalCount = (nNormals == 0 && normals != null ? normals.length
        : nNormals);
    return ms;
  }

  public static MeshSurface newSlab(T3[] vertices, int vertexCount,
                                    float[] vertexValues,
                                    int[][] polygonIndexes, int polygonCount,
                                    int checkCount) {
    // from DRAW only
    MeshSurface ms = new MeshSurface();
    ms.vs = vertices;
    ms.vvs = vertexValues;
    ms.vc = vertexCount;
    ms.pis = polygonIndexes;
    ms.pc = polygonCount;
    ms.checkCount = checkCount; // will be 1
    return ms;
  }

  /**
   * @return The vertices.
   */
  public T3[] getVertices() {
    return (altVertices == null ? vs : altVertices);
  }

  /**
   * @return faces, if defined (in exporter), otherwise polygonIndexes
   */
  public int[][] getFaces() {
    return pis;
  }

  public void setColix(short colix) {
    this.colix = colix;
  }

  public void setColixBack(short colix) {
    this.colixBack = colix;
  }

  public int addV(T3 vertex, boolean asCopy) { //used by mps and surfaceGenerator
    if (vc == 0)
      vs = new T3[SEED_COUNT];
    else if (vc == vs.length)
      vs = (T3[]) AU.doubleLength(vs);
    vs[vc] = (asCopy ? P3.newP(vertex) : vertex);
    return vc++;
  }

  public void addTriangle(int vertexA, int vertexB, int vertexC) {
    addPolygon(new int[] { vertexA, vertexB, vertexC }, null);
  }

  public void addQuad(int vertexA, int vertexB, int vertexC, int vertexD) {
    haveQuads = true;
    addPolygon(new int[] { vertexA, vertexB, vertexC, vertexD }, null);
  }

  public void setPolygonCount(int polygonCount) {
    this.pc = polygonCount;
    if (polygonCount < 0)
      return;
    if (pis == null || polygonCount > pis.length)
      pis = AU.newInt2(polygonCount);
  }

  public int addVCVal(T3 vertex, float value, boolean asCopy) {
    if (vc == 0)
      vvs = new float[SEED_COUNT];
    else if (vc >= vvs.length)
      vvs = AU.doubleLengthF(vvs);
    vvs[vc] = value;
    return addV(vertex, asCopy);
  }

  public int addTriangleCheck(int vertexA, int vertexB, int vertexC, int check,
                              int check2, int color) {
    return (vs == null
        || vvs != null
        && (Float.isNaN(vvs[vertexA]) || Float.isNaN(vvs[vertexB]) || Float
            .isNaN(vvs[vertexC])) || Float.isNaN(vs[vertexA].x)
        || Float.isNaN(vs[vertexB].x) || Float.isNaN(vs[vertexC].x) ? -1
        : addPolygonV3(vertexA, vertexB, vertexC, check, check2, color, null));
  }

  private int addPolygonV3(int vertexA, int vertexB, int vertexC, int check,
                           int check2, int color, BS bs) {
    return (checkCount == 2 ? addPolygonC(new int[] { vertexA, vertexB,
        vertexC, check, check2 }, color, bs) : addPolygon(new int[] { vertexA,
        vertexB, vertexC, check }, bs));
  }

  private int lastColor;
  private short lastColix;

  protected int addPolygonC(int[] polygon, int color, BS bs) {
    if (color != 0) {
      if (pcs == null || pc == 0)
        lastColor = 0;
      short colix = (color == lastColor ? lastColix : (lastColix = C
          .getColix(lastColor = color)));
      setPolygonColix(pc, colix);
    }
    return addPolygon(polygon, bs);
  }

  private int addPolygon(int[] polygon, BS bs) {
    int n = pc;
    if (pc == 0)
      pis = AU.newInt2(SEED_COUNT);
    else if (pc == pis.length)
      pis = (int[][]) AU.doubleLength(pis);
    if (bs != null)
      bs.set(pc);
    pis[pc++] = polygon;
    return n;
  }

  private void setPolygonColix(int index, short colix) {
    if (pcs == null) {
      pcs = new short[SEED_COUNT];
    } else if (index >= pcs.length) {
      pcs = AU.doubleLengthShort(pcs);
    }
    pcs[index] = colix;
  }

  public void invalidatePolygons() {
    for (int i = pc; --i >= mergePolygonCount0;)
      if ((bsSlabDisplay == null || bsSlabDisplay.get(i)) && !setABC(i))
        pis[i] = null;
  }

  protected int iA, iB, iC;

  protected boolean setABC(int i) {
    if (bsSlabDisplay != null && !bsSlabDisplay.get(i)
        && (bsSlabGhost == null || !bsSlabGhost.get(i)))
      return false;
    int[] vertexIndexes = pis[i];
    if (vertexIndexes == null || vertexIndexes.length < 3)
      return false;
    iA = vertexIndexes[0];
    iB = vertexIndexes[1];
    iC = vertexIndexes[2];
    return vvs == null
        || !(Float.isNaN(vvs[iA]) || Float.isNaN(vvs[iB]) || Float
            .isNaN(vvs[iC]));
  }

  public int polygonCount0;
  public int vertexCount0;

  public BS bsSlabDisplay;
  public BS bsSlabGhost;
  public BS bsTransPolygons;
  public int slabMeshType;
  public short slabColix;

  /**
   * Must create bsTransPolygons, polygonTranslucencies, and new triangle set
   * for partially translucent polygons
   * 
   * @param bsVertices
   */
  public void setTranslucentVertices(BS bsVertices) {
    //TODO 

  }

  public void setSlab(BS bsDisplay, BS bsGhost, String type, String color,
                      float translucency) {
    bsSlabDisplay = bsDisplay;
    bsSlabGhost = bsGhost;
    slabMeshType = (type.equalsIgnoreCase("mesh") ? T.mesh : T.fill);
    slabColix = C.getColixTranslucent3(C.getColixS(color), true, translucency);
  }

  public BS bsDisplay;

  public String getSlabColor() {
    return (bsSlabGhost == null ? null : C.getHexCode(slabColix));
  }

  public String getSlabTranslucency() {
    return (bsSlabGhost == null ? null : ""
        + C.getColixTranslucencyFractional(slabColix));
  }

  public String getSlabType() {
    return (bsSlabGhost != null && slabMeshType == T.mesh ? "mesh" : null);
  }

  public SB slabOptions;

  public void resetTransPolygons() {
    boolean isTranslucent = C.isColixTranslucent(colix);
    float translucentLevel = C.getColixTranslucencyFractional(colix);
    for (int i = 0; i < pc; i++)
      if (bsTransPolygons.get(i)) {
        if (!setABC(i))
          continue;
        vcs[iA] = C.getColixTranslucent3(vcs[iA], isTranslucent,
            translucentLevel);
        vcs[iB] = C.getColixTranslucent3(vcs[iB], isTranslucent,
            translucentLevel);
        vcs[iC] = C.getColixTranslucent3(vcs[iC], isTranslucent,
            translucentLevel);
      }
    bsTransPolygons = null;
    polygonTranslucencies = null;
  }

  public void resetSlab() {
    slabPolygons(TempArray.getSlabObjectType(T.none, null, false, null), false);
  }

  /**
   * legacy -- for some scripts with early isosurface slabbing
   * 
   * @param s
   * @param isCap
   * @return slabInfo object
   */
  public static Object[] getCapSlabObject(String s, boolean isCap) {
    try {
      if (s.indexOf("array") == 0) {
        String[] pts = PT.split(s.substring(6, s.length() - 1), ",");
        return TempArray.getSlabObjectType(T.boundbox,
            new P3[] { (P3) Escape.uP(pts[0]), (P3) Escape.uP(pts[1]),
                (P3) Escape.uP(pts[2]), (P3) Escape.uP(pts[3]) }, isCap, null);
      }
      Object plane = Escape.uP(s);
      if (plane instanceof P4)
        return TempArray.getSlabObjectType(T.plane, plane, isCap, null);
    } catch (Exception e) {
      //
    }
    return null;
  }

  public void slabPolygonsList(Lst<Object[]> slabInfo, boolean allowCap) {
    for (int i = 0; i < slabInfo.size(); i++)
      if (!slabPolygons(slabInfo.get(i), allowCap))
        break;
  }

  public boolean slabPolygons(Object[] slabObject, boolean allowCap) {
    if (polygonCount0 < 0)
      return false; // disabled for some surface types
    int slabType = ((Integer) slabObject[0]).intValue();
    if (slabType == T.none || slabType == T.brillouin) {
      if (bsSlabDisplay != null && (polygonCount0 != 0 || vertexCount0 != 0)) {
        pc = polygonCount0;
        vc = vertexCount0;
        polygonCount0 = vertexCount0 = 0;
        normixCount = (isTriangleSet ? pc : vc);
        bsSlabDisplay.setBits(0, (pc == 0 ? vc : pc));
        slabOptions = new SB().append(meshType + " slab none");
        bsSlabGhost = null;
        slabMeshType = T.none;
      }
      if (slabType == T.none)
        return false;
    }
    Object slabbingObject = slabObject[1];
    boolean andCap = ((Boolean) slabObject[2]).booleanValue()
        && !(slabType == T.brillouin);
    if (andCap && !allowCap)
      return false;
    Object[] colorData = (Object[]) slabObject[3];
    boolean isGhost = (colorData != null);
    if (bsSlabDisplay == null || polygonCount0 == 0 && vertexCount0 == 0) {
      polygonCount0 = pc;
      vertexCount0 = vc;
      bsSlabDisplay = BSUtil.setAll(pc == 0 ? vc : pc);
      bsSlabGhost = null;
      if (pc == 0 && vc == 0)
        return false;
    } else if (isMerged) {
      if (pc == 0)
        bsSlabDisplay.setBits(mergeVertexCount0, vc);
      else
        bsSlabDisplay.setBits(mergePolygonCount0, pc);
    }

    if (isGhost) {
      if (bsSlabGhost == null)
        bsSlabGhost = new BS();
      slabMeshType = ((Integer) colorData[0]).intValue();
      slabColix = ((Short) colorData[1]).shortValue();
      //if (C.isColixColorInherited(slabColix))
      //slabColix = C.copyColixTranslucency(slabColix, colix);
      andCap = false;
      colix = C.getColixTranslucent3(colix, false, 0);
    }

    SB sb = new SB();
    sb.append(andCap ? " cap " : " slab ");
    if (isGhost) {
      sb.append("translucent ")
          .appendF(C.getColixTranslucencyFractional(slabColix)).append(" ");
      String s = C.getHexCode(slabColix);
      if (s != null)
        sb.append(s).append(" ");
      if (slabMeshType == T.mesh)
        sb.append("mesh ");
    }
    switch (slabType) {
    case T.brillouin:
      sb.append("brillouin");
      slabBrillouin((P3[]) slabbingObject);
      break;
    case T.decimal:
      getIntersection(0, null, null, null, null, (BS) slabbingObject, null,
          andCap, false, T.decimal, isGhost);
      break;
    case T.plane:
      P4 plane = (P4) slabbingObject;
      sb.append(Escape.eP4(plane));
      getIntersection(0, plane, null, null, null, null, null, andCap, false,
          T.plane, isGhost);
      break;
    case T.unitcell:
    case T.boundbox:
      P3[] box = (P3[]) slabbingObject;
      sb.append("within ").append(Escape.eAP(box));
      P4[] faces = BoxInfo.getFacesFromCriticalPoints(box);
      for (int i = 0; i < faces.length; i++) {
        getIntersection(0, faces[i], null, null, null, null, null, andCap,
            false, T.plane, isGhost);
      }
      break;
    case T.data:
      getIntersection(0, null, null, null, (float[]) slabbingObject, null,
          null, false, false, T.min, isGhost);
      break;
    case T.within:
    case T.range:
    case T.mesh:
      Object[] o = (Object[]) slabbingObject;
      float distance = ((Float) o[0]).floatValue();
      switch (slabType) {
      case T.within:
        P3[] points = (P3[]) o[1];
        BS bs = (BS) o[2];
        sb.append("within ").appendF(distance)
            .append(bs == null ? Escape.e(points) : Escape.e(bs));
        getIntersection(distance, null, points, null, null, null, null, andCap,
            false, (distance > 0 ? T.distance : T.sphere), isGhost);
        break;
      case T.range:
        // isosurface slab within range x.x y.y
        // if y.y < x.x then this effectively means "NOT within range y.y x.x"
        if (vvs == null)
          return false;
        float distanceMax = ((Float) o[1]).floatValue();
        sb.append("within range ").appendF(distance).append(" ")
            .appendF(distanceMax);
        bs = (distanceMax < distance ? BSUtil.copy(bsSlabDisplay) : null);
        getIntersection(distance, null, null, null, null, null, null, andCap,
            false, T.min, isGhost);
        BS bsA = (bs == null ? null : BSUtil.copy(bsSlabDisplay));
        BSUtil.copy2(bs, bsSlabDisplay);
        getIntersection(distanceMax, null, null, null, null, null, null,
            andCap, false, T.max, isGhost);
        if (bsA != null)
          bsSlabDisplay.or(bsA);
        break;
      case T.mesh:
        //NOT IMPLEMENTED
        MeshSurface mesh = (MeshSurface) o[1];
        //distance = -1;
        getIntersection(0, null, null, null, null, null, mesh, andCap, false,
            distance < 0 ? T.min : T.max, isGhost);
        //TODO: unresolved how exactly to store this in the state
        // -- must indicate exact set of triangles to slab and how!
        break;
      }
      break;
    }
    String newOptions = sb.toString();
    if (slabOptions == null)
      slabOptions = new SB();
    if (slabOptions.indexOf(newOptions) < 0)
      slabOptions.append(slabOptions.length() > 0 ? "; " : "").append(meshType)
          .append(newOptions);
    return true;
  }

  /**
   * @param unitCellVectors
   */
  protected void slabBrillouin(P3[] unitCellVectors) {
    // isosurfaceMesh only
    return;
  }

  protected int addIntersectionVertex(T3 vertex, float value, int source,
                                      int set, Map<String, Integer> mapEdge,
                                      int i1, int i2) {

    String key = (i1 > i2 ? i2 + "_" + i1 : i1 + "_" + i2);
    if (i1 >= 0) {
      Integer v = mapEdge.get(key);
      if (v != null) {
        return v.intValue();
      }
    }

    if (vertexSource != null) {
      if (vc >= vertexSource.length)
        vertexSource = AU.doubleLengthI(vertexSource);
      vertexSource[vc] = source;
    }
    if (vertexSets != null) {
      if (vc >= vertexSets.length)
        vertexSets = AU.doubleLengthI(vertexSets);
      vertexSets[vc] = set;
    }
    int i = addVCVal(vertex, value, true);
    //int i = addVertexCopy(vertex, value, -4, true);
    mapEdge.put(key, Integer.valueOf(i));
    return i;
  }

  private boolean doClear;
  private boolean doGhost;
  private boolean doCap;
  private int iD, iE;

  public int mergeVertexCount0;
  public int mergePolygonCount0;
  public boolean isMerged;

  /**
   * @param distance
   *        a distance from a plane or point
   * @param plane
   *        a slabbing plane
   * @param ptCenters
   *        a set of atoms to measure distance from
   * @param vData
   *        when not null, this is a query, not an actual slabbing
   * @param fData
   *        vertex values or other data to overlay
   * @param bsSource
   *        TODO
   * @param meshSurface
   *        second surface; not implemented -- still some problems there
   * @param andCap
   *        to cap this off, crudely only
   * @param doClean
   *        compact set - draw only
   * @param tokType
   *        type of slab
   * @param isGhost
   *        translucent slab, so we mark slabbed triangles
   */
  public void getIntersection(float distance, P4 plane, P3[] ptCenters,
                              Lst<P3[]> vData, float[] fData, BS bsSource,
                              MeshSurface meshSurface, boolean andCap,
                              boolean doClean, int tokType, boolean isGhost) {
    boolean isSlab = (vData == null);
    P3[] pts = null;
    if (fData == null) {
      if (tokType == T.decimal && bsSource != null) {
        if (vertexSource == null)
          return;
        fData = new float[vc];
        for (int i = 0; i < vc; i++)
          if ((fData[i] = vertexSource[i]) == -1)
            System.out.println("meshsurface hmm");
      } else {
        fData = vvs;  
      }
    }
    Map<String, Integer> mapEdge = new Hashtable<String, Integer>();

    /*    
    Vector3f vNorm = null;
    Vector3f vBC = null;
    Vector3f vAC = null;
    Point3f[] pts2 = null;
    Vector3f vTemp3 = null;
    Point4f planeTemp = null;
    boolean isMeshIntersect = (meshSurface != null);
    if (isMeshIntersect) {
      // NOT IMPLEMENTED
      vBC = new Vector3f();
      vAC = new Vector3f();
      vNorm = new Vector3f();
      plane = new Point4f();
      planeTemp = new Point4f();
      vTemp3 = new Vector3f();
      pts2 = new Point3f[] { null, new Point3f(), new Point3f() };
    }
    */
    if (ptCenters != null || isGhost)
      andCap = false; // can only cap faces, and no capping of ghosts
    float[] values = new float[2];
    float[] fracs = new float[2];
    double absD = Math.abs(distance);
    float d1, d2, d3, valA, valB, valC;
    int sourceA = 0, sourceB = 0, sourceC = 0, setA = 0;
    Lst<int[]> iPts = (andCap ? new Lst<int[]>() : null);
    if (pc == 0) {
      for (int i = mergeVertexCount0; i < vc; i++) {
        if (Float.isNaN(fData[i])
            || checkSlab(tokType, vs[i], fData[i], distance, plane, ptCenters,
                bsSource) > 0)
          bsSlabDisplay.clear(i);
      }
      return;
    }
    int iLast = pc;
    for (int i = mergePolygonCount0; i < iLast; i++) {
      if (!setABC(i))
        continue;
      BS bsSlab = (bsSlabGhost != null && bsSlabGhost.get(i) ? bsSlabGhost
          : bsSlabDisplay);
      int check1 = pis[i][3];
      int check2 = (checkCount == 2 ? pis[i][4] : 0);
      T3 vA = vs[iA];
      T3 vB = vs[iB];
      T3 vC = vs[iC];
      valA = fData[iA];
      valB = fData[iB];
      valC = fData[iC];
      if (vertexSource != null) {
        sourceA = vertexSource[iA];
        sourceB = vertexSource[iB];
        sourceC = vertexSource[iC];
      }
      if (vertexSets != null)
        setA = vertexSets[iA];
      d1 = checkSlab(tokType, vA, valA,
          (bsSource == null ? distance : sourceA), plane, ptCenters, bsSource);
      d2 = checkSlab(tokType, vB, valB,
          (bsSource == null ? distance : sourceB), plane, ptCenters, bsSource);
      d3 = checkSlab(tokType, vC, valC,
          (bsSource == null ? distance : sourceC), plane, ptCenters, bsSource);
      int test1 = (d1 != 0 && d1 < 0 ? 1 : 0) + (d2 != 0 && d2 < 0 ? 2 : 0)
          + (d3 != 0 && d3 < 0 ? 4 : 0);

      /*      
            if (iA == 955 || iB == 955 || iC == 955) {
              System.out.println(i + " " + iA + " " + iB + " " + iC + " "+ d1 + " " + d2 + " " + d3 + " " + test1);
              System.out.println("testing messhsurf ");
            }
      */
      /*      
      if (isMeshIntersect && test1 != 7 && test1 != 0) {
        // NOT IMPLEMENTED
        boolean isOK = (d1 == 0 && d2 * d3 >= 0 || d2 == 0 && (d1 * d3) >= 0 || d3 == 0
            && d1 * d2 >= 0);
        if (isOK)
          continue;
        // We have a potential crossing. Now to find the exact point of crossing
        // the other isosurface.
        if (checkIntersection(vA, vB, vC, meshSurface, pts2, vNorm, vBC, vAC,
            plane, planeTemp, vTemp3)) {
          iD = addIntersectionVertex(pts2[0], 0, sourceA, mapEdge, -1, -1); // have to choose some source
          addPolygon(iA, iB, iD, check1 & 1, check2, 0, bsSlabDisplay);
          addPolygon(iD, iB, iC, check1 & 2, check2, 0, bsSlabDisplay);
          addPolygon(iA, iD, iC, check1 & 4, check2, 0, bsSlabDisplay);
          test1 = 0; // toss original    
          iLast = polygonCount;
        } else {
          // process normally for now  
          // not fully implemented -- need to check other way as well.
        }
      }
      */
      switch (test1) {
      default:
      case 7:
      case 0:
        // all on the same side
        break;
      case 1:
      case 6:
        // BC on same side
        if (ptCenters == null)
          pts = new P3[] {
              interpolatePoint(vA, vB, -d1, d2, valA, valB, values, fracs, 0),
              interpolatePoint(vA, vC, -d1, d3, valA, valC, values, fracs, 1) };
        else
          pts = new P3[] {
              interpolateSphere(vA, vB, -d1, -d2, absD, valA, valB, values,
                  fracs, 0),
              interpolateSphere(vA, vC, -d1, -d3, absD, valA, valC, values,
                  fracs, 1) };
        break;
      case 2:
      case 5:
        //AC on same side
        if (ptCenters == null)
          pts = new P3[] {
              interpolatePoint(vB, vA, -d2, d1, valB, valA, values, fracs, 1),
              interpolatePoint(vB, vC, -d2, d3, valB, valC, values, fracs, 0) };
        else
          pts = new P3[] {
              interpolateSphere(vB, vA, -d2, -d1, absD, valB, valA, values,
                  fracs, 1),
              interpolateSphere(vB, vC, -d2, -d3, absD, valB, valC, values,
                  fracs, 0) };
        break;
      case 3:
      case 4:
        //AB on same side need A-C, B-C
        if (ptCenters == null)
          pts = new P3[] {
              interpolatePoint(vC, vA, -d3, d1, valC, valA, values, fracs, 0),
              interpolatePoint(vC, vB, -d3, d2, valC, valB, values, fracs, 1) };
        else
          pts = new P3[] {
              interpolateSphere(vC, vA, -d3, -d1, absD, valC, valA, values,
                  fracs, 0),
              interpolateSphere(vC, vB, -d3, -d2, absD, valC, valB, values,
                  fracs, 1) };
        break;
      }
      doClear = true;
      doGhost = isGhost;
      doCap = andCap;
      BS bs;
      // adjust for minor discrepencies 
      //for (int j = 0; j < 2; j++) 
      //if (fracs[j] == 0)
      //fracs[1 - j] = (fracs[1 - j] < 0.5 ? 0 : 1);

      if (isSlab) {
        //        iD = iE = -1;
        switch (test1) {
        //             A
        //            / \
        //           B---C
        case 0:
          // all on the same side -- toss
          doCap = false;
          break;
        case 7:
          // all on the same side -- keep
          continue;
        case 1:
        case 6:
          //          0  A  0
          //            / \
          //        0 -------- 1 
          //          /     \
          //       1 B-------C  1
          boolean tossBC = (test1 == 1);
          if (tossBC || isGhost) {
            // 1: BC on side to toss -- +tossBC+isGhost  -tossBC+isGhost
            if (!getDE(fracs, 0, iA, iB, iC, tossBC))
              break;
            if (iD < 0)
              iD = addIntersectionVertex(pts[0], values[0], sourceA, setA,
                  mapEdge, iA, iB);
            if (iE < 0)
              iE = addIntersectionVertex(pts[1], values[1], sourceA, setA,
                  mapEdge, iA, iC);
            bs = (tossBC ? bsSlab : bsSlabGhost);
            addPolygonV3(iA, iD, iE, check1 & 5 | 2, check2, 0, bs);
            if (!isGhost)
              break;
          }
          // BC on side to keep -- -tossBC+isGhost,  +tossBC+isGhost
          if (!getDE(fracs, 1, iA, iC, iB, tossBC))
            break;
          bs = (tossBC ? bsSlabGhost : bsSlab);
          if (iE < 0) {
            iE = addIntersectionVertex(pts[0], values[0], sourceB, setA,
                mapEdge, iA, iB);
            addPolygonV3(iE, iB, iC, check1 & 3, check2, 0, bs);
          }
          if (iD < 0) {
            iD = addIntersectionVertex(pts[1], values[1], sourceC, setA,
                mapEdge, iA, iC);
            addPolygonV3(iD, iE, iC, check1 & 4 | 1, check2, 0, bs);
          }
          break;
        case 5:
        case 2:
          //              A
          //            \/ \
          //            /\  \
          //           B--\--C
          //               \
          //
          boolean tossAC = (test1 == 2);
          if (tossAC || isGhost) {
            //AC on side to toss
            if (!getDE(fracs, 0, iB, iC, iA, tossAC))
              break;
            bs = (tossAC ? bsSlab : bsSlabGhost);
            if (iE < 0)
              iE = addIntersectionVertex(pts[0], values[0], sourceB, setA,
                  mapEdge, iB, iA);
            if (iD < 0)
              iD = addIntersectionVertex(pts[1], values[1], sourceB, setA,
                  mapEdge, iB, iC);
            addPolygonV3(iE, iB, iD, check1 & 3 | 4, check2, 0, bs);
            if (!isGhost)
              break;
          }
          // AC on side to keep
          if (!getDE(fracs, 1, iB, iA, iC, tossAC))
            break;
          bs = (tossAC ? bsSlabGhost : bsSlab);
          if (iD < 0) {
            iD = addIntersectionVertex(pts[0], values[0], sourceA, setA,
                mapEdge, iB, iA);
            addPolygonV3(iA, iD, iC, check1 & 5, check2, 0, bs);
          }
          if (iE < 0) {
            iE = addIntersectionVertex(pts[1], values[1], sourceC, setA,
                mapEdge, iB, iC);
            addPolygonV3(iD, iE, iC, check1 & 2 | 1, check2, 0, bs);
          }
          break;
        case 4:
        case 3:
          //              A
          //             / \/
          //            /  /\
          //           B--/--C
          //             /
          //
          boolean tossAB = (test1 == 4);
          if (tossAB || isGhost) {
            if (!getDE(fracs, 0, iC, iA, iB, tossAB))
              break;
            if (iD < 0)
              iD = addIntersectionVertex(pts[0], values[0], sourceC, setA,
                  mapEdge, iA, iC); //CA
            if (iE < 0)
              iE = addIntersectionVertex(pts[1], values[1], sourceC, setA,
                  mapEdge, iB, iC); //CB
            bs = (tossAB ? bsSlab : bsSlabGhost);
            addPolygonV3(iD, iE, iC, check1 & 6 | 1, check2, 0, bs);
            if (!isGhost)
              break;
          }
          //AB on side to keep
          if (!getDE(fracs, 1, iC, iB, iA, tossAB))
            break;
          bs = (tossAB ? bsSlabGhost : bsSlab);
          if (iE < 0) {
            iE = addIntersectionVertex(pts[0], values[0], sourceA, setA,
                mapEdge, iA, iC); //CA
            addPolygonV3(iA, iB, iE, check1 & 5, check2, 0, bs);
          }
          if (iD < 0) {
            iD = addIntersectionVertex(pts[1], values[1], sourceB, setA,
                mapEdge, iB, iC); //CB
            addPolygonV3(iE, iB, iD, check1 & 2 | 4, check2, 0, bs);
          }
          break;
        }
        if (doClear) {
          bsSlab.clear(i);
          if (doGhost)
            bsSlabGhost.set(i);
        }
        if (doCap) {
          iPts.addLast(new int[] { iD, iE });
        }
      } else if (pts != null) {
        vData.addLast(pts);
      }
    }
    if (andCap && iPts.size() > 0) {
      P3 center = new P3();
      for (int i = iPts.size(); --i >= 0;) {
        int[] ipts = iPts.get(i);
        center.add(vs[ipts[0]]);
        center.add(vs[ipts[1]]);
      }
      center.scale(0.5f / iPts.size());
      int v0 = addIntersectionVertex(center, 0, -1, setA, mapEdge, -1, -1);
      for (int i = iPts.size(); --i >= 0;) {
        int[] ipts = iPts.get(i);
        //int p =
        addPolygonV3(ipts[0], v0, ipts[1], 0, 0, 0, bsSlabDisplay);
      }
    }

    if (!doClean)
      return;
    BS bsv = new BS();
    BS bsp = new BS();
    for (int i = 0; i < pc; i++) {
      if (pis[i] == null)
        continue;
      bsp.set(i);
      for (int j = 0; j < 3; j++)
        bsv.set(pis[i][j]);
    }
    int n = 0;
    int nPoly = bsp.cardinality();
    if (nPoly != pc) {
      int[] map = new int[vc];
      for (int i = 0; i < vc; i++)
        if (bsv.get(i))
          map[i] = n++;
      T3[] vTemp = new P3[n];
      n = 0;
      for (int i = 0; i < vc; i++)
        if (bsv.get(i))
          vTemp[n++] = vs[i];
      int[][] pTemp = AU.newInt2(nPoly);
      nPoly = 0;
      for (int i = 0; i < pc; i++)
        if (pis[i] != null) {
          for (int j = 0; j < 3; j++)
            pis[i][j] = map[pis[i][j]];
          pTemp[nPoly++] = pis[i];
        }
      vs = vTemp;
      vc = n;
      pis = pTemp;
      pc = nPoly;
    }
  }

  private static int setPoint(float[] fracs, int i, int i0, int i1) {
    return (fracs[i] == 0 ? i0 : fracs[i] == 1 ? i1 : -1);
  }

  private boolean getDE(float[] fracs, int fD, int i1, int i2, int i3,
                        boolean toss23) {

    //          0 (1) 0
    //            / \
    //     iD 0 -fracs- 1 iE 
    //          /     \
    //      1 (2)-------(3) 1
    iD = setPoint(fracs, fD, i1, i2);
    iE = setPoint(fracs, 1 - fD, i1, i3);

    // initially: doClear=true, doCap = andCap, doGhost = isGhost

    if (iD == i1 && iE == i1) {
      // toss all if tossing 23, otherwise ignore
      doClear = toss23;
      doCap = false;
      return false;
    }
    if (iD == i2 && iE == i3) {
      // cap but don't toss if tossing 23
      doClear = !toss23;
      return false;
    }
    if (iD == i1 || iE == i1) {
      // other is i2 or i3 -- along an edge
      // cap but toss all if tossing 23
      doClear = toss23;
      if (iD < 0) {
        iD = (toss23 ? i2 : i3);
      } else if (iE < 0) {
        iE = (toss23 ? i3 : i2);
      }
      return doCap;
    }

    doGhost = false;
    return true;
  }

  private static float checkSlab(int tokType, T3 v, float val, float distance,
                                 P4 plane, T3[] ptCenters, BS bs) {
    float d;
    switch (tokType) {
    case T.decimal:
      return (val >= 0 && bs.get((int) val) ? 1 : -1);
    case T.min:
      d = distance - val;
      break;
    case T.max:
      d = val - distance;
      break;
    case T.plane:
      d = Measure.distanceToPlane(plane, v);
      break;
    case T.distance:
      d = minDist(v, ptCenters) - distance;
      break;
    default:
      d = -minDist(v, ptCenters) - distance;
      break;
    }
    return (Math.abs(d) < 0.0001f ? 0 : d);
  }

  /*
    private static boolean checkIntersection(Point3f vA, Point3f vB, Point3f vC,
                                      MeshSurface meshSurface, Point3f[] pts, 
                                      Vector3f vNorm, Vector3f vAB, Vector3f vAC, Point4f plane, Point4f pTemp, Vector3f vTemp3) {
      
      Measure.getPlaneThroughPoints(vA, vB, vC, vNorm, vAB, vAC, plane);
      for (int i = 0; i < meshSurface.polygonCount; i++) {
        Point3f pt = meshSurface.getTriangleIntersection(i, vA, vB, vC, plane, vNorm, vAB, pts[1], pts[2], vAC, pTemp, vTemp3);
        if (pt != null) {
          pts[0] = Point3f.new3(pt);
          return true; 
        }
      }
      return false;
    }
    
    private Point3f getTriangleIntersection(int i, Point3f vA, Point3f vB, Point3f vC, Point4f plane, Vector3f vNorm, Vector3f vTemp, 
                                            Point3f ptRet, Point3f ptTemp, Vector3f vTemp2, Point4f pTemp, Vector3f vTemp3) {
      return (setABC(i) ? Measure.getTriangleIntersection(vA, vB, vC, plane, vertices[iA], vertices[iB], vertices[iC], vNorm, vTemp, ptRet, ptTemp, vTemp2, pTemp, vTemp3) : null);
    }
  */

  private static float minDist(T3 pt, T3[] ptCenters) {
    float dmin = Integer.MAX_VALUE;
    for (int i = ptCenters.length; --i >= 0;) {
      float d = ptCenters[i].distance(pt);
      if (d < dmin)
        dmin = d;
    }
    return dmin;
  }

  private P3 interpolateSphere(T3 v1, T3 v2, float d1, float d2, double absD,
                               float val1, float val2, float[] values,
                               float[] fracs, int i) {
    return interpolateFraction(v1, v2,
        getSphericalInterpolationFraction(absD, d1, d2, v1.distance(v2)), val1,
        val2, values, fracs, i);
  }

  private static P3 interpolatePoint(T3 v1, T3 v2, float d1, float d2,
                                     float val1, float val2, float[] values,
                                     float[] fracs, int i) {
    return interpolateFraction(v1, v2, d1 / (d1 + d2), val1, val2, values,
        fracs, i);
  }

  private static P3 interpolateFraction(T3 v1, T3 v2, float f, float val1,
                                        float val2, float[] values,
                                        float[] fracs, int i) {
    if (f < 0.0001)
      f = 0;
    else if (f > 0.9999)
      f = 1;
    fracs[i] = f;
    values[i] = (val2 - val1) * f + val1;
    return P3.new3(v1.x + (v2.x - v1.x) * f, v1.y + (v2.y - v1.y) * f, v1.z
        + (v2.z - v1.z) * f);
  }

  public static float getSphericalInterpolationFraction(double r,
                                                        double valueA,
                                                        double valueB, double d) {
    double ra = Math.abs(r + valueA) / d;
    double rb = Math.abs(r + valueB) / d;
    r /= d;
    double ra2 = ra * ra;
    double q = ra2 - rb * rb + 1;
    double p = 4 * (r * r - ra2);
    double factor = (ra < rb ? 1 : -1);
    return (float) (((q) + factor * Math.sqrt(q * q + p)) / 2);
  }

  /**
   * Calculates the data (faces, vertices, normals) for a sphere.
   * 
   * @param lvl
   * 
   * @return The data.
   */
  public static MeshSurface getSphereData(int lvl) {
    // _ObjExporter only
    Geodesic.createGeodesic(lvl);
    int vertexCount = Geodesic.getVertexCount(lvl);
    short[] f = Geodesic.getFaceVertexes(lvl);
    int nFaces = f.length / 3;
    int[][] faces = AU.newInt2(nFaces);
    for (int i = 0, fpt = 0; i < nFaces; i++) {
      faces[i] = new int[] { f[fpt++], f[fpt++], f[fpt++] };
    }
    V3[] vectors = new V3[vertexCount];
    for (int i = 0; i < vertexCount; i++)
      vectors[i] = Geodesic.getVertexVector(i);
    return newMesh(true, vectors, 0, faces, vectors, 0);
  }

}
